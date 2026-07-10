package ru.practicum.request.service;



import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.RequestStatus;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;

import java.util.List;
import java.util.Optional;

public interface RequestService {
    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequest);

    List<ParticipationRequestDto> findAllByEventIdInAndStatus(List<Long> eventsId, RequestStatus status);

    Long findCountByEventIdInAndStatus(Long eventId, RequestStatus status);

    Optional<ParticipationRequestDto> findByRequesterIdAndEventId(Long userId, Long eventId);
}