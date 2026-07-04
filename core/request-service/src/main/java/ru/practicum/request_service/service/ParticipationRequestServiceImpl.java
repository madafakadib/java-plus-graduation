package ru.practicum.request_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.client.EventClient;
import ru.practicum.common.client.UserClient;
import ru.practicum.common.eventDto.dto.EventFullDto;
import ru.practicum.common.eventDto.dto.enums.EventState;
import ru.practicum.common.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.common.requestDto.dto.EventRequestStatusUpdateRequest;
import ru.practicum.common.requestDto.dto.EventRequestStatusUpdateResult;
import ru.practicum.common.requestDto.dto.ParticipationRequestDto;
import ru.practicum.common.requestDto.model.RequestStatus;
import ru.practicum.request_service.mapper.ParticipationRequestMapper;
import ru.practicum.request_service.model.ParticipationRequest;
import ru.practicum.request_service.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final EventClient eventClient;
    private final UserClient userClient;
    private final ParticipationRequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConditionsNotMetException("Request already exists for this event");
        }

        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        if (!eventClient.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        EventFullDto event = eventClient.getEventById(eventId);
        if (event == null) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Initiator can't add request to his own event");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsNotMetException("Impossible to add request to not published event");
        }

        if (event.getParticipantLimit() != 0) {
            long participants = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (participants >= event.getParticipantLimit()) {
                throw new ConditionsNotMetException("The limit of participation requests has been reached: " + participants);
            }
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .eventId(eventId)
                .requesterId(userId)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        }

        request = requestRepository.save(request);
        log.info("Created participation request: userId={}, eventId={}, status={}", userId, eventId, request.getStatus());

        return requestMapper.toParticipationRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getUserParticipationRequests(Long userId) {
        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);
        if (requests.isEmpty()) {
            return List.of();
        }

        return requestMapper.toParticipationRequestDto(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequesterId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " not found for user with id=" + userId);
        }

        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.CONFIRMED) {
            throw new ConditionsNotMetException(
                    "Only requests with PENDING or CONFIRMED status can be canceled. Current status: " + request.getStatus()
            );
        }

        request.setStatus(RequestStatus.CANCELED);
        request = requestRepository.save(request);

        log.info("Canceled participation request: userId={}, requestId={}", userId, requestId);

        return requestMapper.toParticipationRequestDto(request);
    }

    @Override
    public long countConfirmedRequests(Long eventId) {
        if (!eventClient.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEventId(Long eventId) {
        if (!eventClient.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        if (requests.isEmpty()) {
            return List.of();
        }

        return requestMapper.toParticipationRequestDto(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        if (!eventClient.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        EventFullDto event = eventClient.getEventById(eventId);
        if (event == null) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        List<ParticipationRequest> requests = requestRepository.findByIdIn(updateRequest.getRequestIds());

        for (ParticipationRequest r : requests) {
            if (!r.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConditionsNotMetException("Only requests with PENDING status can be reviewed");
            }
            if (!r.getEventId().equals(eventId)) {
                throw new ConditionsNotMetException("The requests are not related to event with id = " + eventId);
            }
        }

        List<ParticipationRequest> approved = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        long confirmedRequests = 0L;
        long limit = event.getParticipantLimit();

        if ((!event.getRequestModeration() || event.getParticipantLimit() == 0)
                && updateRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            approved = requests;
        } else if (updateRequest.getStatus().equals(RequestStatus.REJECTED)) {
            rejected = requests;
        } else {
            confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

            for (ParticipationRequest r : requests) {
                if (confirmedRequests < limit) {
                    approved.add(r);
                    confirmedRequests++;
                } else {
                    rejected.add(r);
                }
            }
        }

        updateStatuses(approved, RequestStatus.CONFIRMED);
        updateStatuses(rejected, RequestStatus.REJECTED);

        if (limit > 0 && confirmedRequests >= limit) {
            requestRepository.updateStatusByEventId(eventId, RequestStatus.PENDING, RequestStatus.REJECTED);
        }

        log.info("Updated request statuses for event: {}, approved: {}, rejected: {}",
                eventId, approved.size(), rejected.size());

        return requestMapper.toEventRequestStatusUpdateResult(approved, rejected);
    }

    private void updateStatuses(List<ParticipationRequest> requests, RequestStatus status) {
        if (requests.isEmpty()) {
            return;
        }

        List<Long> ids = requests.stream()
                .map(ParticipationRequest::getId)
                .collect(Collectors.toList());

        int updated = requestRepository.updateStatusByIdIn(ids, status);
        if (updated != ids.size()) {
            throw new IllegalStateException(String.format(
                    "Failed to update all requests in the database. Total: %d, updated: %d",
                    ids.size(), updated));
        }

        requests.forEach(r -> r.setStatus(status));
    }
}