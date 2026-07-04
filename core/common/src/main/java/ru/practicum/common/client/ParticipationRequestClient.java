package ru.practicum.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.common.requestDto.dto.EventRequestStatusUpdateRequest;
import ru.practicum.common.requestDto.dto.EventRequestStatusUpdateResult;
import ru.practicum.common.requestDto.dto.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service", path = "/api/internal/requests")
public interface ParticipationRequestClient {

    @GetMapping("/event/{eventId}/confirmed/count")
    long countConfirmedRequests(@PathVariable("eventId") Long eventId);

    @GetMapping("/event/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventId(@PathVariable("eventId") Long eventId);

    @PutMapping("/event/{eventId}/status")
    EventRequestStatusUpdateResult updateRequestStatuses(
            @PathVariable("eventId") Long eventId,
            @RequestBody EventRequestStatusUpdateRequest updateRequest);
}