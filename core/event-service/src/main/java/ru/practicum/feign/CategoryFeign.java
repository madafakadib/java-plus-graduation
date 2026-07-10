package ru.practicum.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.CategoryDto;

import java.util.Map;
import java.util.Set;
/**
 * OpenFeign-клиент для взаимодействия с {@code category-service}.
 * <p>Используется для получения категорий событий из внешнего микросервиса.</p>
 *
 * <ul>
 *   <li>{@link #getCategoryById(Long)} — получить категорию по её идентификатору;</li>
 *   <li>{@link #getCategoryById(Set)} — получить несколько категорий по их id (bulk-запрос).</li>
 * </ul>
 *
 * <p>Маршруты совпадают с публичным API {@code category-service}.</p>
 */
@FeignClient(name = "category-service")
public interface CategoryFeign {
    /**
     * Получить категорию по идентификатору.
     *
     * @param id идентификатор категории
     * @return DTO категории
     */
    @GetMapping("/categories/{id}")
    CategoryDto getCategoryById(@PathVariable Long id);
    /**
     * Получить несколько категорий по их идентификаторам.
     *
     * @param categoriesId множество идентификаторов категорий
     * @return мапа вида {@code id -> CategoryDto}
     */
    @PostMapping("/categories/map")
    Map<Long, CategoryDto> getCategoryById(@RequestBody Set<Long> categoriesId);
}
