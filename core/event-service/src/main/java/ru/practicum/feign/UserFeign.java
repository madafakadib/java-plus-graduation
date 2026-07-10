package ru.practicum.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.UserShortDto;

import java.util.Map;
import java.util.Set;

/**
 * OpenFeign-клиент для взаимодействия с {@code user-service}.
 * <p>Используется для получения краткой информации о пользователях.</p>
 *
 * <ul>
 *   <li>{@link #findUserShortDtoById(Long)} — получить краткое представление пользователя по id;</li>
 *   <li>{@link #findUserShortDtoById(Set)} — получить краткие данные сразу по множеству пользователей.</li>
 * </ul>
 */
@FeignClient(name = "user-service")
public interface UserFeign {
    /**
     * Получить краткое представление пользователя.
     *
     * @param userId идентификатор пользователя
     * @return DTO с краткими данными (id, имя)
     */
    @GetMapping("/admin/users/short/{userId}")
    UserShortDto findUserShortDtoById(@PathVariable("userId") Long userId);

    /**
     * Получить краткие данные о нескольких пользователях.
     *
     * @param usersId множество идентификаторов пользователей
     * @return мапа вида {@code userId -> UserShortDto}
     */
    @PostMapping("/admin/users/short/map")
    Map<Long, UserShortDto> findUserShortDtoById(@RequestBody Set<Long> usersId);
}