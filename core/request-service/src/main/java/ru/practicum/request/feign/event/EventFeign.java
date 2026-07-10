package ru.practicum.request.feign.event;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.EventFullDto;

import java.util.Optional;

@FeignClient(name = "event-service")
public interface EventFeign {
    @GetMapping("/users/{userId}/events/optional/{eventId}")
    Optional<EventFullDto> findOptEventByIdAndUserId(@PathVariable(name = "userId") Long userId,
                                                     @PathVariable(name = "eventId") Long eventId);

    @GetMapping("/admin/events/{eventId}")
    EventFullDto findEventById(@PathVariable(name = "eventId") Long eventId);
}