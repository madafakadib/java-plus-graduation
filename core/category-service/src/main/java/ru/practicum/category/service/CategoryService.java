package ru.practicum.category.service;


import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.dto.CategoryDto;

import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Сервисный слой для работы с категориями событий.
 * <p>Инкапсулирует бизнес-логику: CRUD-операции и выборки для публичного/админского API.</p>
 *
 * <p>Основные сценарии:</p>
 * <ul>
 *     <li>Постраничное чтение и выборка по id;</li>
 *     <li>Создание новой категории (имя уникально);</li>
 *     <li>Обновление и удаление (при удалении проверяется отсутствие связанных событий);</li>
 *     <li>Массовая выборка по множеству id.</li>
 * </ul>
 *
 * <p>Исключения:
 * <ul>
 *     <li>{@code CategoryNotFoundException} — категория не найдена;</li>
 *     <li>{@code DataIntegrityViolationException} — при нарушении уникальности имени или связях с событиями.</li>
 * </ul>
 * </p>
 */
public interface CategoryService {
    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getCategoryById(Long id);

    Map<Long, CategoryDto> getCategoryById(Set<Long> categoriesId);

    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long id);

    CategoryDto updateCategory(Long id, CategoryDto categoryDto);
}