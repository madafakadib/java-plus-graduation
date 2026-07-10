package ru.practicum.request.service;




import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.*;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.exception.*;
import ru.practicum.request.feign.event.EventFeign;
import ru.practicum.request.feign.user.UserFeign;
import ru.practicum.request.model.Request;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final UserFeign userFeign;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final EventFeign eventFeign;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        findUser(userId);
        List<Request> requests = requestRepository.findByRequesterId(userId);
        log.info("Результат поиска в БД: {}", requests);
        List<ParticipationRequestDto> result = requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
        log.info("Результат маппинга: {}", result);
        return result;
    }

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        UserShortDto user = findUser(userId);
        if (findEventByIdAndUserId(eventId, userId).isPresent()) {
            throw new InitiatorRequestException("Пользователь с ID - " + userId + ", является создателем события с ID - " + eventId);
        }
        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new RepeatUserRequestorException("Пользователь с ID - " + userId + ", уже заявился на событие с ID - " + eventId + ".");
        }
        EventFullDto event = findEventById(eventId);
        if (!event.getState().equals(EventState.PUBLISHED.name())) {
            throw new NotPublishEventException("Данное событие ещё не опубликовано");
        }

        Request request = new Request();
        request.setRequesterId(userId);
        request.setEventId(event.getId());

        Long confirmedRequests = requestRepository.countRequestsByEventAndStatus(event.getId(), RequestStatus.CONFIRMED);
        if (confirmedRequests >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            throw new ParticipantLimitException("Достигнут лимит участников для данного события.");
        }

        request.setCreatedOn(LocalDateTime.now());
        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            request = requestRepository.save(request);
            log.info("Сохраняем запрос в БД: {}", request);
            ParticipationRequestDto result = requestMapper.toParticipationRequestDto(request);
            log.info("Результат маппинга: {}", result);
            return result;
        }

        if (event.getRequestModeration()) {
            request.setStatus(RequestStatus.PENDING);
            request = requestRepository.save(request);
            log.info("Сохраняем запрос в БД: {}", request);
            ParticipationRequestDto result = requestMapper.toParticipationRequestDto(request);
            log.info("Результат маппинга: {}", result);
            return result;
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        request = requestRepository.save(request);
        log.info("Сохраняем запрос в БД: {}", request);
        ParticipationRequestDto result = requestMapper.toParticipationRequestDto(request);
        log.info("Результат маппинга: {}", result);
        return result;
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        findUser(userId);
        Request cancelRequest = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Запрос с ID - " + requestId + ", не найден."));
        log.info("Результат поиска запрос в БД: {}", cancelRequest);
        cancelRequest.setStatus(RequestStatus.CANCELED);
        cancelRequest = requestRepository.save(cancelRequest);
        log.info("Сохраняем запрос в БД: {}", cancelRequest);
        ParticipationRequestDto result = requestMapper.toParticipationRequestDto(cancelRequest);
        log.info("Результат маппинга: {}", result);
        return result;
    }

    @Override
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        findUser(userId);
        EventFullDto event = findEventById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Пользователь с ID - " + userId + ", не является инициатором события с ID - " + eventId + ".");
        }
        List<Request> requests = requestRepository.findByEventId(event.getId());
        log.info("Результат поиска запросов в БД: {}", requests);
        List<ParticipationRequestDto> result = requestMapper.toParticipationRequestDto(requests);
        log.info("Результат маппинга: {}", result);
        return result;
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest eventRequest) {
        findUser(userId);
        EventFullDto event = findEventByIdAndUserId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Событие с ID - " + eventId + ", пользователя с ID - " + userId + ", не найдено."));
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new OperationUnnecessaryException("Запрос составлен некорректно.");
        }

        List<Long> requestIds = eventRequest.getRequestIds();
        List<Request> requests = requestIds.stream()
                .map(r -> requestRepository.findByIdAndEventId(r, eventId)
                        .orElseThrow(() -> new ValidationException("Запрос с ID - " + r + ", не найден.")))
                .toList();
        log.info("Результат поиска запросов в БД: {}", requests);
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        Long confirmedRequestsCount = requestRepository.countRequestsByEventAndStatus(event.getId(), RequestStatus.CONFIRMED);

        if (confirmedRequestsCount >= event.getParticipantLimit()) {
            throw new ParticipantLimitException("Достигнут лимит участников для данного события.");
        }

        List<Request> updatedRequests = new ArrayList<>();

        for (Request request : requests) {
            if (request.getStatus().equals(RequestStatus.PENDING)) {
                if (eventRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
                    if (confirmedRequestsCount <= event.getParticipantLimit()) {
                        request.setStatus(RequestStatus.CONFIRMED);
                        updatedRequests.add(request);
                        confirmedRequestsCount++;
                    } else {
                        request.setStatus(RequestStatus.REJECTED);
                        updatedRequests.add(request);
                    }
                } else {
                    request.setStatus(eventRequest.getStatus());
                    updatedRequests.add(request);
                }
            }
        }

        List<Request> savedRequests = requestRepository.saveAll(updatedRequests);
        log.info("Сохраняем обновленные запросы в БД: {}", savedRequests);
        for (Request request : savedRequests) {
            if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
                confirmedRequests.add(requestMapper.toParticipationRequestDto(request));
            } else {
                rejectedRequests.add(requestMapper.toParticipationRequestDto(request));
            }
        }

        EventRequestStatusUpdateResult resultRequest = new EventRequestStatusUpdateResult();
        resultRequest.setConfirmedRequests(confirmedRequests);
        resultRequest.setRejectedRequests(rejectedRequests);
        log.info("Результат обновления запросов: {}", resultRequest);
        return resultRequest;
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdInAndStatus(List<Long> eventsId, RequestStatus status) {
        List<Request> requests = requestRepository.findAllByEventIdInAndStatus(eventsId, status);
        log.info("Результат поиска запросов в БД: {}", requests);
        List<ParticipationRequestDto> result = requestMapper.toParticipationRequestDto(requests);
        log.info("Результат маппинга: {}", result);
        return result;
    }

    @Override
    public Long findCountByEventIdInAndStatus(Long eventId, RequestStatus status) {
        Long result = requestRepository.countRequestsByEventAndStatus(eventId, status);
        log.info("Количество запросов к событию с указанным статусом: {}", result);
        return result;
    }

    @Override
    public Optional<ParticipationRequestDto> findByRequesterIdAndEventId(Long userId, Long eventId) {
        Optional<Request> requestOpt = requestRepository.findByRequesterIdAndEventId(userId, eventId);
        log.info("Результат поиска запросов в БД: {}", requestOpt);
        if (requestOpt.isEmpty()) {
            log.info("Запрос не найден");
            return Optional.empty();
        }
        ParticipationRequestDto dto = requestMapper.toParticipationRequestDto(requestOpt.get());
        log.info("Результат маппинга: {}", dto);
        return Optional.of(dto);
    }

    private UserShortDto findUser(Long userId) {
        try {
            UserShortDto dto = userFeign.findUserShortDtoById(userId);
            log.info("Результат поиска user-service: {}", dto);
            return dto;
        } catch (FeignException e) {
            throw new EntityNotFoundException("Пользователь c ID - " + userId + ", не найден.");
        }
    }

    private Optional<EventFullDto> findEventByIdAndUserId(Long eventId, Long userId) {
        try {
            Optional<EventFullDto> dto = eventFeign.findOptEventByIdAndUserId(userId, eventId);
            log.info("Результат поиска в event-service: {}", dto);
            return dto;
        } catch (FeignException e) {
            throw new EntityNotFoundException("Ошибка работы с event-service");
        }
    }

    private EventFullDto findEventById(Long eventId) {
        try {
            EventFullDto dto = eventFeign.findEventById(eventId);
            log.info("Результат поиска в event-service: {}", dto);
            return dto;
        } catch (FeignException e) {
            throw new EntityNotFoundException("Событие c ID - " + eventId + ", не найдено.");
        }
    }
}