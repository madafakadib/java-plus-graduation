package ru.practicum.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.category.service.CategoryService;
import ru.practicum.dto.CategoryDto;

/**
 * Админский REST-контроллер для управления категориями событий.
 * <p>
 * Операции:
 * <ul>
 *     <li>POST — создание новой категории;</li>
 *     <li>DELETE — удаление (если не связана с событиями);</li>
 *     <li>PATCH — обновление имени/описания.</li>
 * </ul>
 *
 * <p>Валидация запросов через {@link jakarta.validation.Valid}.
 * Логирование всех вызовов — через {@code Slf4j}.
 *
 *
 * @see ru.practicum.category.service.CategoryService
 */
@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Запрос на создание категории: {}", newCategoryDto);
        return categoryService.createCategory(newCategoryDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable Long id) {
        log.info("Запрос на удаление категории: {}", id);
        categoryService.deleteCategory(id);
    }

    @PatchMapping("/{id}")
    public CategoryDto updateCategory(@PathVariable Long id,
                                      @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Запрос на обновление категории: id:{}, categoryDto: {}", id, categoryDto);
        return categoryService.updateCategory(id, categoryDto);
    }
}