package ru.practicum.category_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category_service.service.InternalCategoryService;
import ru.practicum.common.categoryDto.dto.CategoryDto;

@Slf4j
@RestController
@RequestMapping("/api/internal/categories")
@RequiredArgsConstructor
public class InternalCategoryController {

    private final InternalCategoryService internalCategoryService;

    @GetMapping("/{categoryId}")
    public CategoryDto getCategoryById(@PathVariable("categoryId") Long categoryId) {
        log.info("Internal API: Getting category by id: {}", categoryId);
        return internalCategoryService.getCategoryById(categoryId);
    }
}