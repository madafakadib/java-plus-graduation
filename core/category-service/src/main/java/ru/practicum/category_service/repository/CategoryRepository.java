package ru.practicum.category_service.repository;


import ru.practicum.category_service.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category postCategory(Category category);

    void deleteCategory(Long catId);

    Category patchCategory(Long catId, Category category);

    List<Category> getCategories(int from, int size);

    Category getCategory(Long catId);

    boolean existsById(Long catId);

    Optional<Category> findById(Long catId);
}
