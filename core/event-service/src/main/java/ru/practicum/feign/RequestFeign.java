package ru.practicum.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.RequestStatus;

import java.util.List;
import java.util.Optional;
/**
 * OpenFeign-клиент для взаимодействия с {@code request-service}.
 * <p>Используется для получения заявок на участие в событиях.</p>
 *
 * <ul>
 *   <li>{@link #findAllByEventIdInAndStatus(List, RequestStatus)} —
 *   получить список заявок по событиям и статусу;</li>
 *   <li>{@link #findCountByEventIdInAndStatus(Long, RequestStatus)} —
 *   получить количество заявок для события по статусу;</li>
 *   <li>{@link #findByRequesterIdAndEventId(Long, Long)} —
 *   найти заявку конкретного пользователя на конкретное событие.</li>
 * </ul>
 */
@FeignClient(name = "request-service")
public interface RequestFeign {
    /**
     * Получить все заявки для списка событий по статусу.
     *
     * @param eventsId список идентификаторов событий
     * @param status   статус заявок (например, {@code CONFIRMED})
     * @return список DTO заявок
     */
    @GetMapping("requests/events/{eventId}/{status}")
    List<ParticipationRequestDto> findAllByEventIdInAndStatus(@PathVariable(name = "eventId") List<Long> eventsId,
                                                              @PathVariable RequestStatus status);
    /**
     * Получить количество заявок для события по статусу.
     *
     * @param eventId идентификатор события
     * @param status  статус заявок
     * @return количество заявок
     */
    @GetMapping("requests/events/{eventId}/{status}/count")
    Long findCountByEventIdInAndStatus(@PathVariable Long eventId, @PathVariable RequestStatus status);
    /**
     * Найти заявку пользователя на участие в событии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор события
     * @return заявка, если существует
     */
    @GetMapping("/users/{userId}/events/{eventId}/requests/requester")
    Optional<ParticipationRequestDto> findByRequesterIdAndEventId(@PathVariable Long userId, @PathVariable Long eventId);
}