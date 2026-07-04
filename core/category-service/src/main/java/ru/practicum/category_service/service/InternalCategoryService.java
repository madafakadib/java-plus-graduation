package ru.practicum.category_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.common.categoryDto.dto.CategoryDto;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.category_service.mapper.CategoryMapper;
import ru.practicum.category_service.model.Category;
import ru.practicum.category_service.repository.CategoryRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalCategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryDto getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
        return CategoryMapper.toCategoryDto(category);
    }
}