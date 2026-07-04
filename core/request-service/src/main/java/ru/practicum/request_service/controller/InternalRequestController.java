package ru.practicum.request_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.common.requestDto.dto.EventRequestStatusUpdateRequest;
import ru.practicum.common.requestDto.dto.EventRequestStatusUpdateResult;
import ru.practicum.common.requestDto.dto.ParticipationRequestDto;
import ru.practicum.request_service.service.ParticipationRequestService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/internal/requests")
@RequiredArgsConstructor
public class InternalRequestController {

    private final ParticipationRequestService requestService;

    @GetMapping("/event/{eventId}/confirmed/count")
    public long countConfirmedRequests(@PathVariable("eventId") Long eventId) {
        log.info("Internal API: Counting confirmed requests for event: {}", eventId);
        return requestService.countConfirmedRequests(eventId);
    }

    @GetMapping("/event/{eventId}")
    public List<ParticipationRequestDto> getRequestsByEventId(@PathVariable("eventId") Long eventId) {
        log.info("Internal API: Getting requests for event: {}", eventId);
        return requestService.getRequestsByEventId(eventId);
    }

    @PutMapping("/event/{eventId}/status")
    public EventRequestStatusUpdateResult updateRequestStatuses(
            @PathVariable("eventId") Long eventId,
            @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("Internal API: Updating request statuses for event: {}", eventId);
        return requestService.updateRequestStatuses(eventId, updateRequest);
    }
}