package ru.practicum.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.common.eventDto.dto.EventFullDto;
import ru.practicum.common.eventDto.dto.EventShortDto;

import java.util.List;

@FeignClient(name = "event-service")
public interface EventClient {
    @GetMapping("/{eventId}")
    boolean existsById(@PathVariable("eventId") Long eventId);

    @GetMapping("/exists-by-category")
    boolean existsByCategoryId(@RequestParam("categoryId") Long categoryId);

    @GetMapping("/{eventId}")
    EventFullDto getEventById(@PathVariable("eventId") Long eventId);

    @GetMapping("/short-dtos")
    List<EventShortDto> getShortDtosByIds(@RequestParam("ids") List<Long> ids);
}

