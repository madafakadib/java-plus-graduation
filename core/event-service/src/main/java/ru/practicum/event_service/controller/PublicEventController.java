package ru.practicum.event_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.common.eventDto.dto.EventFullDto;
import ru.practicum.common.eventDto.dto.EventShortDto;
import ru.practicum.common.eventDto.dto.paramDto.PublicUserEventParam;
import ru.practicum.event_service.service.EventService;

import java.util.List;

@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@Valid PublicUserEventParam param,
                                         HttpServletRequest request) {

        param.setUri(request.getRequestURI());
        param.setIp(request.getRemoteAddr());

        log.debug("Public request to get events: {}", param);

        return eventService.getEventsForPublicRequests(param);
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable long id, HttpServletRequest request) {

        log.debug("Request to get event: uri={}, ip={}, id={}", request.getRequestURI(), request.getRemoteAddr(), id);

        return eventService.findEventById(request.getRequestURI(), request.getRemoteAddr(), id);
    }


}
