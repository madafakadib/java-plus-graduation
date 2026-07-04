package ru.practicum.event_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.common.eventDto.dto.EventFullDto;
import ru.practicum.common.eventDto.dto.EventShortDto;
import ru.practicum.event_service.service.InternalEventService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/internal/events")
@RequiredArgsConstructor
public class InternalEventController {

    private final InternalEventService internalEventService;

    @GetMapping("/{eventId}/exist")
    public boolean existById(@PathVariable("eventId") Long eventId) {
        return internalEventService.existsById(eventId);
    }

    @GetMapping("/exists-by-category")
    public boolean existsByCategoryId(@RequestParam("categoryId") Long categoryId) {
        return internalEventService.existsByCategoryId(categoryId);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventById(@PathVariable("eventId") Long eventId) {
        return internalEventService.getEventById(eventId);
    }

    @GetMapping("/short-dtos")
    public List<EventShortDto> getShortDtosByIds(@RequestParam("ids") List<Long> ids) {
        log.info("Internal API: Getting event short DTOs for ids: {}", ids);
        return internalEventService.getShortDtosByIds(ids);
    }
}