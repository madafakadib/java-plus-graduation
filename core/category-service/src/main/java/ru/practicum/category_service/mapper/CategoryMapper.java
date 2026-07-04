package ru.practicum.category_service.mapper;

import ru.practicum.category_service.model.Category;
import ru.practicum.common.categoryDto.dto.CategoryDto;
import ru.practicum.common.categoryDto.dto.NewCategoryDto;

public class CategoryMapper {
    public static Category toCategory(CategoryDto categoryDto) {
        return Category.builder()
                .id(categoryDto.getId())
                .name(categoryDto.getName())
                .build();
    }

    public static Category toCategory(NewCategoryDto newCategoryDto) {
        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

    public static CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static NewCategoryDto toNewCategoryDto(Category category) {
        return NewCategoryDto.builder()
                .name(category.getName())
                .build();
    }
}
