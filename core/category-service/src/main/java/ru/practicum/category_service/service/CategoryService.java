package ru.practicum.category_service.service;


import ru.practicum.common.categoryDto.dto.CategoryDto;
import ru.practicum.common.categoryDto.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto postCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long catId);

    CategoryDto patchCategory(Long catId, NewCategoryDto newCategoryDto);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(Long catId);

}
