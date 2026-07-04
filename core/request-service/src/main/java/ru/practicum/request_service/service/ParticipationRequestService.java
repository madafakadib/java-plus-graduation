package ru.practicum.request_service.service;

import ru.practicum.common.requestDto.dto.EventRequestStatusUpdateRequest;
import ru.practicum.common.requestDto.dto.EventRequestStatusUpdateResult;
import ru.practicum.common.requestDto.dto.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getUserParticipationRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    long countConfirmedRequests(Long eventId);

    List<ParticipationRequestDto> getRequestsByEventId(Long eventId);

    EventRequestStatusUpdateResult updateRequestStatuses(Long eventId, EventRequestStatusUpdateRequest updateRequest);
}
