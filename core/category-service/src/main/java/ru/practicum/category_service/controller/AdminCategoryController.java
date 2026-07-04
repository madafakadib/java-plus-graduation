package ru.practicum.category_service.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category_service.service.CategoryService;
import ru.practicum.common.categoryDto.dto.CategoryDto;
import ru.practicum.common.categoryDto.dto.NewCategoryDto;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto postCategory(@RequestBody @Valid NewCategoryDto newCategoryDto) {
        return categoryService.postCategory(newCategoryDto);
    }

    @DeleteMapping(path = "/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
    }

    @PatchMapping(path = "/{catId}")
    public CategoryDto patchCategory(@PathVariable Long catId,
                                     @RequestBody @Valid NewCategoryDto newCategoryDto) {
        return categoryService.patchCategory(catId, newCategoryDto);
    }

}
