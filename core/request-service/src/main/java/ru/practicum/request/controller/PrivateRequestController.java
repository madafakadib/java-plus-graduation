package ru.practicum.request.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.CollectorClient;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.RequestStatus;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.service.RequestService;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateRequestController {

    private final RequestService requestService;
    private final CollectorClient collectorClient;

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        log.info("Запрос на получение запросов пользователя: {}", userId);
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                                 @RequestParam Long eventId) {
        log.info("Запрос на создание запроса пользователем: {}, в событии: {}", userId, eventId);
        ParticipationRequestDto result = requestService.createRequest(userId, eventId);
        collectorClient.sendRegistration(userId, eventId);
        return result;
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Запрос на закрытие пользователем: {}, его запроса: {}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос на получение пользователем: {}, всех запросов к событию: {}", userId, eventId);
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateStatusRequest(@PathVariable Long userId, @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest eventRequest) {
        log.info("Запрос на обновление статусов запросов пользователем: {}, к событию: {}", userId, eventId);
        return requestService.updateStatusRequest(userId, eventId, eventRequest);
    }

    @GetMapping("requests/events/{eventId}/{status}")
    public List<ParticipationRequestDto> findAllByEventIdInAndStatus(@PathVariable(name = "eventId") List<Long> eventsId,
                                                                     @PathVariable RequestStatus status) {
        log.info("Запрос на поиск всех запросов событий: {}, со статусом: {}", eventsId, status);
        return requestService.findAllByEventIdInAndStatus(eventsId, status);
    }

    @GetMapping("requests/events/{eventId}/{status}/count")
    public Long findCountByEventIdInAndStatus(@PathVariable Long eventId, @PathVariable RequestStatus status) {
        log.info("Запрос на поиск количества запросов события: {}, со статусом: {}", eventId, status);
        return requestService.findCountByEventIdInAndStatus(eventId, status);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests/requester")
    public Optional<ParticipationRequestDto> findByRequesterIdAndEventId(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Запрос на поиск запроса пользователя: {}, к событию: {}", userId, eventId);
        return requestService.findByRequesterIdAndEventId(userId, eventId);
    }
}