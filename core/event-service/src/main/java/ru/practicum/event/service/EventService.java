package ru.practicum.event.service;


import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.event.dto.*;

import java.util.List;
import java.util.Optional;

/**
 * Сервисный слой для работы с событиями.
 * <p>Инкапсулирует бизнес-логику публичного, приватного и административного API.</p>
 *
 * <h3>Публичное API:</h3>
 * <ul>
 *   <li>{@link #getAllEvents(ReqParam)} — поиск событий с фильтрацией и пагинацией;</li>
 *   <li>{@link #publicGetEvent(long)} — получение события по id (детали);</li>
 *   <li>{@link #likeEvent(Long, Long)} — поставить лайк событию;</li>
 *   <li>{@link #getRecommendations(Long, Integer)} — рекомендации событий для пользователя.</li>
 * </ul>
 *
 * <h3>Приватное API (пользовательское):</h3>
 * <ul>
 *   <li>{@link #create(Long, NewEventDto)} — создание нового события инициатором;</li>
 *   <li>{@link #findUserEvents(Long, Integer, Integer)} — список событий пользователя;</li>
 *   <li>{@link #findUserEventById(Long, Long)} — событие пользователя по id;</li>
 *   <li>{@link #updateEventByUser(Long, Long, UpdateEventUserRequest)} — обновление события инициатором;</li>
 *   <li>{@link #findOptEventByUserIdAndId(Long, Long)} — поиск события пользователя как {@link Optional}.</li>
 * </ul>
 *
 * <h3>Административное API:</h3>
 * <ul>
 *   <li>{@link #getAllEvents(AdminEventParams)} — поиск событий с расширенными фильтрами (по пользователям, категориям, состояниям);</li>
 *   <li>{@link #update(Long, UpdateEventAdminRequest)} — обновление события администратором (публикация, отклонение);</li>
 *   <li>{@link #findAllByCategoryId(Long, Integer, Integer)} — поиск событий по категории (для проверки связей);</li>
 *   <li>{@link #findEventById(Long)} — поиск события по id без ограничений.</li>
 * </ul>
 *
 * <p>Работает с DTO уровня API:
 * {@link ru.practicum.dto.EventShortDto}, {@link ru.practicum.dto.EventFullDto},
 * {@link NewEventDto}, {@link UpdateEventUserRequest}, {@link UpdateEventAdminRequest},
 * {@link AdminEventParams}, {@link ReqParam}.</p>
 */
public interface EventService {
    // --- Публичное API ---
    List<EventShortDto> getAllEvents(ReqParam reqParam);

    EventFullDto publicGetEvent(long id);

    void likeEvent(Long userId, Long eventId);

    List<EventFullDto> getRecommendations(Long userId, Integer maxResults);

    // --- Приватное API ---


    EventFullDto create(Long userId, NewEventDto newEventDto);

    List<EventShortDto> findUserEvents(Long userId, Integer from, Integer size);

    EventFullDto findUserEventById(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    Optional<EventFullDto> findOptEventByUserIdAndId(Long userId, Long eventId);

    // --- Админское API ---
    List<EventFullDto> findAllByCategoryId(Long categoryId, Integer from, Integer size);

    EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);


    EventFullDto findEventById(Long eventId);

    List<EventFullDto> getAllEvents(AdminEventParams params);

}