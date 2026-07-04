package ru.practicum.event_service.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.eventDto.dto.EventFullDto;
import ru.practicum.common.eventDto.dto.UpdateEventAdminRequest;
import ru.practicum.common.eventDto.dto.paramDto.AdminUserEventParam;
import ru.practicum.event_service.service.EventService;

import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/events")
public class AdminEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEvents(@Valid AdminUserEventParam param) {
        log.debug("Admin request to get events: {}", param);
        return eventService.getEventsForAdminRequests(param);
    }


    // Валидация данных не требуется, по спецификации
    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(
            @PathVariable long eventId,
            @RequestBody @Valid UpdateEventAdminRequest body) {
        log.debug("Admin request to update event:  eventId={}", eventId);

        return eventService.updateEvent(eventId, body);
    }
}
