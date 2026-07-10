package ru.practicum.category.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.EventFullDto;

import java.util.List;
/**
 * Feign-клиент для обращения к сервису событий.
 * <p>Используется в административных сценариях для проверки,
 * есть ли события в категории.</p>
 *
 * <ul>
 *   <li>GET /admin/events/check/category — возвращает список событий
 *       по идентификатору категории с пагинацией.</li>
 * </ul>
 *
 * @see ru.practicum.dto.EventFullDto
 */
@FeignClient(name = "event-service")
public interface EventFeign {
    @GetMapping("/admin/events/check/category")
    List<EventFullDto> adminGetAllEventsByCategory(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size);
}