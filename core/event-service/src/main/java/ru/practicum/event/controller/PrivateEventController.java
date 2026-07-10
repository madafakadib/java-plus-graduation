package ru.practicum.event.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.service.EventService;

import java.util.Collection;
import java.util.Optional;
/**
 * Приватный REST-контроллер для управления событиями пользователем.
 * <p>Доступен авторизованным пользователям, позволяет создавать и изменять собственные события,
 * а также просматривать их список и детали.</p>
 *
 * <ul>
 *   <li>POST /users/{userId}/events — создать новое событие;</li>
 *   <li>GET /users/{userId}/events — получить список собственных событий (постранично);</li>
 *   <li>GET /users/{userId}/events/{eventId} — получить событие по id;</li>
 *   <li>GET /users/{userId}/events/optional/{eventId} — получить событие как {@link Optional} (для внутренних сценариев);</li>
 *   <li>PATCH /users/{userId}/events/{eventId} — обновить данные события.</li>
 * </ul>
 *
 * <p>Используются DTO уровня API:
 * {@link ru.practicum.dto.EventFullDto}, {@link ru.practicum.dto.EventShortDto},
 * {@link ru.practicum.event.dto.NewEventDto}, {@link ru.practicum.event.dto.UpdateEventUserRequest}.</p>
 *
 * <p>Бизнес-логика реализована в {@link ru.practicum.event.service.EventService}.
 * Все вызовы логируются через {@code Slf4j}.</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {
    private final EventService service;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public EventFullDto create(@PathVariable(name = "userId") Long userId,
                               @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Запрос на создание нового события: {}, {}", userId, newEventDto);
        return service.create(userId, newEventDto);
    }

    @GetMapping
    public Collection<EventShortDto> findUserEvents(
            @PathVariable(name = "userId") Long userId,
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        return service.findUserEvents(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto findUserEventById(@PathVariable(name = "userId") Long userId,
                                          @PathVariable(name = "eventId") Long eventId) {
        return service.findUserEventById(userId, eventId);
    }

    @GetMapping("/optional/{eventId}")
    public Optional<EventFullDto> findOptEventByIdAndUserId(@PathVariable(name = "userId") Long userId,
                                                            @PathVariable(name = "eventId") Long eventId) {
        return service.findOptEventByUserIdAndId(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUser(@PathVariable(name = "userId") Long userId,
                                          @PathVariable(name = "eventId") Long eventId,
                                          @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        return service.updateEventByUser(userId, eventId, updateRequest);
    }
}