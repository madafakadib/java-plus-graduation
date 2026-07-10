package ru.practicum.category.service;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.exception.CategoryNotFoundException;
import ru.practicum.category.feign.EventFeign;
import ru.practicum.category.model.Category;
import ru.practicum.category.model.CategoryMapper;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.EventFullDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
/**
 * Реализация {@link CategoryService}.
 * <p>Отвечает за бизнес-логику категорий событий:
 * хранение в БД, преобразование в DTO и проверку зависимостей с событиями.</p>
 *
 * <p>Используемые технологии:</p>
 * <ul>
 *   <li>{@link CategoryRepository} — доступ к таблице категорий;</li>
 *   <li>{@link CategoryMapper} — преобразование между сущностью и DTO;</li>
 *   <li>{@link EventFeign} — проверка связанных событий в сервисе событий.</li>
 * </ul>
 *
 * <p>Особенности:</p>
 * <ul>
 *   <li>При удалении категории проверяет отсутствие связанных событий через event-service;</li>
 *   <li>При нарушении связей или уникальности выбрасывает {@link org.springframework.dao.DataIntegrityViolationException};</li>
 *   <li>При отсутствии категории выбрасывает {@link ru.practicum.category.exception.CategoryNotFoundException}.</li>
 * </ul>
 *
 * <p>Все операции логируются через {@code Slf4j}.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventFeign eventFeign;

    private static final Integer FROM = 0;
    private static final Integer SIZE = 3;

    public List<CategoryDto> getAll(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        log.info("Категории из БД: {}", categories);
        List<CategoryDto> categoriesDto = categoryMapper.toCategoryDtoList(categories);
        log.info("Результат маппинга: {}", categoriesDto);
        return categoriesDto;
    }

    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Категория с ID - " + id + ", не найдена."));
        log.info("Категория из БД: {}", category);
        CategoryDto categoryDto = categoryMapper.toCategoryDto(category);
        log.info("Результат маппинга: {}", categoryDto);
        return categoryDto;
    }

    public Map<Long, CategoryDto> getCategoryById(Set<Long> categoriesId) {
        List<Category> categories = categoryRepository.findAllById(categoriesId);
        if (categories.isEmpty()) {
            throw new CategoryNotFoundException("Категории не найдены.");
        }

        Map<Long, CategoryDto> result = categoryMapper.toCategoryDtoList(categories).stream()
                .collect(Collectors.toMap(CategoryDto::getId, Function.identity()));

        if (categoriesId.size() > result.size()) {
            List<Long> notAvailabilityCategory = new ArrayList<>();
            for (Long id : categoriesId) {
                if (!result.containsKey(id)) {
                    notAvailabilityCategory.add(id);
                }
            }
            throw new CategoryNotFoundException("Не найдены категории с id: " + notAvailabilityCategory);
        }
        log.info("Результат поиска категорий по id: {}", result);
        return result;
    }

    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = categoryMapper.toCategoryByNew(newCategoryDto);
        log.info("Результат маппинга: {}", category);

        category = categoryRepository.save(category);
        log.info("Сохраняем категорию в БД: {}", category);

        CategoryDto categoryDto = categoryMapper.toCategoryDto(category);
        log.info("Результат маппинга: {}", categoryDto);

        return categoryDto;
    }

    public void deleteCategory(Long id) {
        List<EventFullDto> eventFullDtos;
        try {
            eventFullDtos = eventFeign.adminGetAllEventsByCategory(id, FROM, SIZE);
            log.info("События из event-service: {}", eventFullDtos);
        } catch (FeignException e) {
            throw new DataIntegrityViolationException("Возможно есть зависимые события в event-service. " + e.getMessage());
        }
        if (!eventFullDtos.isEmpty()) {
            throw new DataIntegrityViolationException("Есть зависимые события в event-service: " + eventFullDtos);
        }
        categoryRepository.deleteById(id);
    }

    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Категория с ID - " + id + ", не найдена."));
        log.info("Старая категория из БД: {}", category);

        category.setName(categoryDto.getName());
        category = categoryRepository.save(category);
        log.info("Обновляем категорию в БД: {}", category);

        CategoryDto result = categoryMapper.toCategoryDto(category);
        log.info("Результат маппинга: {}", categoryDto);
        return result;
    }
}