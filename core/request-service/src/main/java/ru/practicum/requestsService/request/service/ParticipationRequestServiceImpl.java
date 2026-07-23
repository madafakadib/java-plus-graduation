package ru.practicum.requestsService.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import feign.FeignException;
import ru.practicum.common.dto.events.EventBaseDto;
import ru.practicum.common.dto.events.EventState;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.common.dto.participationRequest.RequestStatus;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.common.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.requestsService.request.client.EventClient;
import ru.practicum.requestsService.request.client.UserClient;
import ru.practicum.requestsService.request.dto.ParticipationRequestMapper;
import ru.practicum.common.dto.participationRequest.RegistrationRequest;
import ru.practicum.requestsService.request.model.ParticipationRequest;
import ru.practicum.requestsService.request.repository.ParticipationRequestRepository;
import ru.practicum.stat.client.CollectorClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository requestRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final CollectorClient collectorClient;

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (eventId == null || eventId <= 0) {
            throw new ConditionsNotMetException("Event with id=" + eventId + " not found");
        }

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConditionsNotMetException("Request already exists for this event");
        }

        UserShortDto user;
        try {
            user = userClient.getUserShortById(userId);
        } catch (FeignException.NotFound e) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        if (user == null) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        EventBaseDto event;
        try {
            event = eventClient.getBaseEventInfo(eventId);
        } catch (FeignException.NotFound e) {
            throw new ConditionsNotMetException("Event with id=" + eventId + " not found");
        }

        if (event == null) {
            throw new ConditionsNotMetException("Event with id=" + eventId + " not found");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Initiator can`t add request to his own event");
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

        ParticipationRequest request = ParticipationRequestMapper.toParticipationRequest(event.getId(), user.getId());
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        request = requestRepository.save(request);

        if (request.getStatus() == RequestStatus.CONFIRMED) {
            collectorClient.sendRegistration(userId, eventId);
            log.info("Отправлена регистрация в Collector: userId={}, eventId={}", userId, eventId);
        }

        return ParticipationRequestMapper.toParticipationRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getUserParticipationRequests(Long userId) {
        List<ParticipationRequest> requests = requestRepository.findByRequesterId(userId);

        if (requests.isEmpty()) {
            return List.of();
        }

        return ParticipationRequestMapper.toParticipationRequestDto(requests);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {

        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));

        if (!request.getRequesterId().equals(userId)) {
            throw new NotFoundException("Request with id=" + requestId + " not found for user with id=" + userId);
        }

        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.CONFIRMED) {
            throw new ConditionsNotMetException("Only requests with PENDING or CONFIRMED status can be canceled. Current status: " + request.getStatus());
        }

        request.setStatus(RequestStatus.CANCELED);

        request = requestRepository.save(request);

        return ParticipationRequestMapper.toParticipationRequestDto(request);
    }


    @Override
    public long getConfirmedRequestsCount(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEventId(Long eventId) {
        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return ParticipationRequestMapper.toParticipationRequestDto(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(
            Long eventId,
            int limit,
            EventRequestStatusUpdateRequest updateRequest
    ) {
        List<ParticipationRequest> requests = requestRepository.findByIdIn(updateRequest.getRequestIds());

        if (requests.isEmpty()) {
            throw new NotFoundException("Requests not found for ids: " + updateRequest.getRequestIds());
        }

        for (ParticipationRequest r : requests) {
            if (!r.getEventId().equals(eventId)) {
                throw new ConditionsNotMetException("Request with id=" + r.getId() + " is not related to event=" + eventId);
            }
            if (!r.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConditionsNotMetException("Only PENDING requests can be reviewed");
            }
        }

        List<ParticipationRequest> approved = new ArrayList<>();
        List<ParticipationRequest> rejected = new ArrayList<>();

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (updateRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
            for (ParticipationRequest r : requests) {
                if (confirmedCount < limit) {
                    approved.add(r);
                    confirmedCount++;
                } else {
                    rejected.add(r);
                }
            }
        } else {
            rejected.addAll(requests);
        }

        if (!approved.isEmpty()) {
            List<Long> approvedIds = approved.stream()
                    .map(ParticipationRequest::getId)
                    .collect(Collectors.toList());
            requestRepository.updateStatusByIdIn(approvedIds, RequestStatus.CONFIRMED);
            approved.forEach(r -> r.setStatus(RequestStatus.CONFIRMED));
        }

        if (!rejected.isEmpty()) {
            List<Long> rejectedIds = rejected.stream()
                    .map(ParticipationRequest::getId)
                    .collect(Collectors.toList());
            requestRepository.updateStatusByIdIn(rejectedIds, RequestStatus.REJECTED);
            rejected.forEach(r -> r.setStatus(RequestStatus.REJECTED));
        }

        requestRepository.saveAll(approved);
        requestRepository.saveAll(rejected);

        if (!approved.isEmpty()) {
            List<RegistrationRequest> registrations = approved.stream()
                    .map(r -> new RegistrationRequest(r.getRequesterId(), r.getEventId()))
                    .collect(Collectors.toList());
            collectorClient.sendRegistrationsBatch(registrations);
        }

        if (limit > 0 && confirmedCount >= limit) {
            List<ParticipationRequest> pendingRequests =
                    requestRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);

            if (!pendingRequests.isEmpty()) {
                List<Long> pendingIds = pendingRequests.stream()
                        .map(ParticipationRequest::getId)
                        .collect(Collectors.toList());
                requestRepository.updateStatusByIdIn(pendingIds, RequestStatus.REJECTED);
                pendingRequests.forEach(r -> r.setStatus(RequestStatus.REJECTED));
                requestRepository.saveAll(pendingRequests);
            }
        }

        return ParticipationRequestMapper.toEventRequestStatusUpdateResult(approved, rejected);
    }

    @Override
    public Map<Long, Long> getConfirmedRequestsCounts(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> counts = requestRepository.countByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);
        return counts.stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));
    }

    private void updateStatuses(List<ParticipationRequest> requests, RequestStatus status) {
        if (requests.isEmpty()) {
            return;
        }
        List<Long> ids = requests.stream()
                .map(ParticipationRequest::getId)
                .collect(Collectors.toList());
        requestRepository.updateStatusByIdIn(ids, status);
        requests.forEach(r -> r.setStatus(status));
    }
}