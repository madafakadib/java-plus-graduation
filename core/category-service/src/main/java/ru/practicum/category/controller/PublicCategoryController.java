package ru.practicum.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.category.service.CategoryService;
import ru.practicum.dto.CategoryDto;

import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * Публичный REST-контроллер для чтения категорий событий.
 * <p>Доступен без административных прав, только операции получения.</p>
 *
 * <ul>
 *     <li>GET /categories — постраничное получение списка категорий;</li>
 *     <li>GET /categories/{id} — получение категории по идентификатору;</li>
 *     <li>POST /categories/map — пакетное получение категорий по id (список id передаётся в теле запроса).</li>
 * </ul>
 *
 * <p>Логирование вызовов выполняется через {@code Slf4j}.</p>
 *
 * @see ru.practicum.category.service.CategoryService
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(path = "/categories")
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAll(@RequestParam(defaultValue = "0") Integer from,
                                    @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрос на получение категорий: from:{}, size:{}", from, size);
        return categoryService.getAll(from, size);
    }

    @GetMapping("/{id}")
    public CategoryDto getCategoryById(@PathVariable Long id) {
        log.info("Запрос на получение категории по id: {}", id);
        return categoryService.getCategoryById(id);
    }

    @PostMapping("/map")
    public Map<Long, CategoryDto> getCategoryById(@RequestBody Set<Long> categoriesId) {
        log.info("Запрос на получение категорий по id: {}", categoriesId);
        return categoryService.getCategoryById(categoriesId);
    }
}