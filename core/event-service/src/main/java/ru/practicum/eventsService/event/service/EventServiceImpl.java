package ru.practicum.eventsService.event.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.dto.comments.CommentStatus;
import ru.practicum.common.dto.events.*;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateRequest;
import ru.practicum.common.dto.participationRequest.EventRequestStatusUpdateResult;
import ru.practicum.common.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.common.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.eventsService.categories.model.Category;
import ru.practicum.eventsService.categories.repository.CategoryRepository;
import ru.practicum.eventsService.client.CommentClient;
import ru.practicum.eventsService.client.ParticipationRequestClient;
import ru.practicum.eventsService.client.UserClient;
import ru.practicum.eventsService.event.dto.EventMapper;
import ru.practicum.eventsService.event.dto.NewEventDto;
import ru.practicum.eventsService.event.dto.UpdateEventAdminRequest;
import ru.practicum.eventsService.event.dto.UpdateEventUserRequest;
import ru.practicum.eventsService.event.dto.paramDto.AdminUserEventParam;
import ru.practicum.eventsService.event.dto.paramDto.EventRepositoryParam;
import ru.practicum.eventsService.event.dto.paramDto.PublicUserEventParam;
import ru.practicum.eventsService.event.model.AdminStateAction;
import ru.practicum.eventsService.event.model.Event;
import ru.practicum.eventsService.event.model.EventSort;
import ru.practicum.eventsService.event.model.UserStateAction;
import ru.practicum.eventsService.event.repository.EventRepository;
import ru.practicum.ewm.stats.messages.RecommendedEventProto;
import ru.practicum.stat.client.AnalyzerClient;
import ru.practicum.stat.client.CollectorClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserClient userClient;
    private final ParticipationRequestClient requestClient;
    private final CommentClient commentClient;
    private final CollectorClient collectorClient;
    private final AnalyzerClient analyzerClient;

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        validateEventDate(newEventDto.getEventDate(), 2);

        UserShortDto userDto = getUserById(userId);
        Category cat = categoryRepository.getCategory(newEventDto.getCategory());

        Event event = EventMapper.toEvent(newEventDto, cat, userId);
        event = eventRepository.save(event);

        return EventMapper.toEventFullDto(event, 0L, 0.0, userDto);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        EventRepositoryParam param = EventRepositoryParam.builder()
                .users(List.of(userId))
                .from(from)
                .size(size)
                .build();

        List<EventShortDto> events = eventRepository.findEventsShortDto(param);
        if (events.isEmpty()) {
            return events;
        }

        enrichEventsWithRating(events);
        enrichEventsWithUsers(events);
        enrichEventsListWithCommentsCount(events);
        enrichEventsWithConfirmedRequests(events);

        return events;
    }

    @Override
    public List<EventShortDto> getEventsForPublicRequests(PublicUserEventParam userEventParam) {
        EventRepositoryParam param = EventRepositoryParam.fromUserEventParam(userEventParam);

        List<EventShortDto> events = eventRepository.findEventsShortDto(param);
        if (events.isEmpty()) {
            return events;
        }

        if (param.isOnlyAvailable()) {
            events = filterAvailableEvents(events);
        }

        enrichEventsWithRating(events);
        enrichEventsWithUsers(events);
        enrichEventsListWithCommentsCount(events);
        enrichEventsWithConfirmedRequests(events);

        if (param.getSortOrDefault() == EventSort.VIEWS) {
            events.sort(Comparator.comparing(EventShortDto::getRating, Comparator.nullsLast(Double::compareTo)).reversed());
        }

        return events;
    }

    @Override
    public List<EventFullDto> getEventsForAdminRequests(AdminUserEventParam adminParam) {
        EventRepositoryParam param = EventRepositoryParam.fromAdminEventParam(adminParam);

        List<EventFullDto> events = eventRepository.findEventsFullDto(param);
        if (events.isEmpty()) {
            return events;
        }

        enrichEventsWithRating(events);
        enrichEventsWithUsers(events);
        enrichEventsListWithCommentsCount(events);
        enrichEventsWithConfirmedRequests(events);

        return events;
    }

    @Override
    public EventFullDto findUserEventByEventId(Long userId, Long eventId) {
        EventFullDto event = findEventFullDtoById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        enrichEvent(event);
        return event;
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest body) {
        Event event = getEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsNotMetException("Only events with CANCELED or PENDING state can be updated");
        }

        validateEventDateForUpdate(event, body.getEventDate());

        if (body.getStateAction() != null) {
            updateEventState(event, body.getStateAction());
        }

        Category cat = null;
        if (body.getCategory() != null) {
            cat = categoryRepository.getCategory(body.getCategory());
        }
        EventMapper.updateEventFromUserRequest(body, event, cat);

        event = eventRepository.save(event);

        return buildEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest body) {
        Event event = getEventById(eventId);

        Category category = null;
        if (body.getCategory() != null) {
            category = categoryRepository.getCategory(body.getCategory());
        }

        EventMapper.updateEventFromAdminRequest(body, event, category);

        if (body.getStateAction() != null) {
            processAdminEventAction(event, body.getStateAction());
        }

        event = eventRepository.save(event);

        return buildEventFullDto(event);
    }

    @Override
    public EventFullDto findEventById(String uri, String ip, Long id) {
        throw new UnsupportedOperationException("Use findEventById with userId parameter");
    }

    public EventFullDto findEventById(Long eventId, Long userId, String ip) {
        EventFullDto event = findEventFullDtoById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Published Event with id=" + eventId + " was not found");
        }

        collectorClient.sendView(userId, eventId);

        double rating = getEventRating(eventId);
        event.setRating(rating);

        enrichEvent(event);

        return event;
    }

    @Override
    public EventFullDto findPublicEventById(Long eventId, String ip) {
        EventFullDto event = findEventFullDtoById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Published Event with id=" + eventId + " was not found");
        }

        try {
            collectorClient.sendView(0L, eventId);
        } catch (Exception e) {
            log.warn("Failed to send view for event {}: {}", eventId, e.getMessage());
        }

        double rating = getEventRating(eventId);
        event.setRating(rating);

        enrichEvent(event);

        return event;
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId) {
        Event event = getEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        return requestClient.getRequestsByEventId(eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId,
                                                                EventRequestStatusUpdateRequest updateRequest) {
        Event event = getEventById(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        return requestClient.updateRequestStatuses(eventId, event.getParticipantLimit(), updateRequest);
    }

    @Override
    public List<EventShortDto> getShortDtosByIds(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findAllByIdIn(eventIds);

        List<EventShortDto> dtos = events.stream()
                .map(event -> EventMapper.toEventShortDto(event, 0L, 0.0))
                .collect(Collectors.toList());

        enrichEventsWithRating(dtos);
        enrichEventsWithUsers(dtos);
        enrichEventsListWithCommentsCount(dtos);
        enrichEventsWithConfirmedRequests(dtos);

        return dtos;
    }

    public List<EventShortDto> getRecommendationsForUser(Long userId, int limit) {
        try {
            List<RecommendedEventProto> recommendations = analyzerClient.getRecommendations(userId, limit);

            if (recommendations.isEmpty()) {
                return Collections.emptyList();
            }

            List<Long> eventIds = recommendations.stream()
                    .map(RecommendedEventProto::getEventId)
                    .collect(Collectors.toList());

            List<Event> events = eventRepository.findAllByIdIn(eventIds);

            Map<Long, Double> scores = recommendations.stream()
                    .collect(Collectors.toMap(
                            RecommendedEventProto::getEventId,
                            RecommendedEventProto::getScore
                    ));

            List<EventShortDto> result = events.stream()
                    .map(event -> {
                        double score = scores.getOrDefault(event.getId(), 0.0);
                        return EventMapper.toEventShortDto(event, 0L, score);
                    })
                    .sorted((e1, e2) -> Double.compare(
                            e2.getRating() != null ? e2.getRating() : 0.0,
                            e1.getRating() != null ? e1.getRating() : 0.0
                    ))
                    .collect(Collectors.toList());

            enrichEventsWithUsers(result);
            enrichEventsListWithCommentsCount(result);
            enrichEventsWithConfirmedRequests(result);

            return result;

        } catch (Exception e) {
            log.error("Ошибка получения рекомендаций", e);
            return Collections.emptyList();
        }
    }

    @Transactional
    public void likeEvent(Long eventId, Long userId) {
        Event event = getEventById(eventId);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsNotMetException("Cannot like unpublished event");
        }

        boolean hasVisited = checkUserVisitedEvent(userId, eventId);

        if (!hasVisited) {
            throw new RuntimeException("User must visit event before liking it. userId=" + userId + ", eventId=" + eventId);
        }

        collectorClient.sendLike(userId, eventId);
    }

    public List<EventShortDto> getSimilarEvents(Long eventId, Long userId, int limit) {
        try {
            List<RecommendedEventProto> similar = analyzerClient.getSimilarEvents(eventId, userId, limit);

            if (similar.isEmpty()) {
                return Collections.emptyList();
            }

            List<Long> eventIds = similar.stream()
                    .map(RecommendedEventProto::getEventId)
                    .collect(Collectors.toList());

            List<Event> events = eventRepository.findAllByIdIn(eventIds);

            Map<Long, Double> scores = similar.stream()
                    .collect(Collectors.toMap(
                            RecommendedEventProto::getEventId,
                            RecommendedEventProto::getScore
                    ));

            List<EventShortDto> result = events.stream()
                    .map(event -> {
                        double score = scores.getOrDefault(event.getId(), 0.0);
                        return EventMapper.toEventShortDto(event, 0L, score);
                    })
                    .sorted((e1, e2) -> Double.compare(
                            e2.getRating() != null ? e2.getRating() : 0.0,
                            e1.getRating() != null ? e1.getRating() : 0.0
                    ))
                    .collect(Collectors.toList());

            enrichEventsWithUsers(result);
            enrichEventsListWithCommentsCount(result);
            enrichEventsWithConfirmedRequests(result);

            return result;

        } catch (Exception e) {
            log.error("Ошибка получения похожих мероприятий", e);
            return Collections.emptyList();
        }
    }

    private double getEventRating(Long eventId) {
        try {
            Map<Long, Double> interactions = analyzerClient.getInteractionsCount(List.of(eventId));
            return interactions.getOrDefault(eventId, 0.0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void enrichEventsWithRating(List<? extends Enrichable> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        List<Long> eventIds = events.stream()
                .map(Enrichable::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (eventIds.isEmpty()) {
            return;
        }

        try {
            Map<Long, Double> ratings = analyzerClient.getInteractionsCount(eventIds);
            events.forEach(event ->
                    event.setRating(ratings.getOrDefault(event.getId(), 0.0))
            );
        } catch (Exception e) {
            events.forEach(event -> event.setRating(0.0));
        }
    }

    private void enrichEventsWithConfirmedRequests(List<? extends Enrichable> events) {
        if (events.isEmpty()) {
            return;
        }

        Map<Long, Long> counts = getConfirmedRequestsCounts(events);
        events.forEach(event ->
                event.setConfirmedRequests(counts.getOrDefault(event.getId(), 0L))
        );
    }

    private void enrichEventsListWithCommentsCount(List<? extends Enrichable> eventDtos) {
        if (eventDtos.isEmpty()) {
            return;
        }

        List<Long> ids = eventDtos.stream()
                .map(Enrichable::getId)
                .collect(Collectors.toList());

        try {
            Map<Long, Long> countsMap = commentClient.getCommentCountsByEventIds(ids, CommentStatus.APPROVED);
            eventDtos.forEach(item -> item.setCommentsCount(countsMap.getOrDefault(item.getId(), 0L)));
        } catch (FeignException e) {
            log.error("Failed to get comments counts for eventIds={}, status={}", ids, e.status());
            eventDtos.forEach(item -> item.setCommentsCount(0L));
        }
    }

    private void enrichEventsWithUsers(List<? extends Enrichable> events) {
        if (events.isEmpty()) {
            return;
        }

        Map<Long, UserShortDto> userMap = getUsersDataMap(events);
        if (userMap.isEmpty()) {
            return;
        }

        events.forEach(event -> {
            UserShortDto currentInitiator = event.getInitiator();
            if (currentInitiator == null || currentInitiator.getId() == null) {
                return;
            }
            UserShortDto enrichedUser = userMap.get(currentInitiator.getId());
            if (enrichedUser != null) {
                event.setInitiator(enrichedUser);
            } else {
                currentInitiator.setName("Unknown");
            }
        });
    }

    private boolean checkUserVisitedEvent(Long userId, Long eventId) {
        return true;
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private EventFullDto findEventFullDtoById(Long eventId) {
        return eventRepository.findEventByIdFullDto(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private UserShortDto getUserById(Long userId) {
        UserShortDto user = userClient.getUserShortById(userId);
        if (user == null) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }
        return user;
    }

    private Map<Long, UserShortDto> getUsersDataMap(List<? extends Enrichable> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        Set<Long> userIds = events.stream()
                .map(Enrichable::getInitiator)
                .filter(Objects::nonNull)
                .map(UserShortDto::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, UserShortDto> userMap = userClient.getUsersDataByIds(new ArrayList<>(userIds));
        return userMap != null ? userMap : Map.of();
    }

    private void validateEventDate(LocalDateTime eventDate, int hours) {
        LocalDateTime minDate = LocalDateTime.now().plusHours(hours);
        if (eventDate.isBefore(minDate)) {
            throw new ConditionsNotMetException("Event date must be at least " + hours + " hours from now");
        }
    }

    private void validateEventDateForUpdate(Event event, LocalDateTime newEventDate) {
        LocalDateTime minEventDateForUpdating = LocalDateTime.now().plusHours(2);
        if (event.getEventDate().isBefore(minEventDateForUpdating)) {
            throw new ConditionsNotMetException("Unable to update event at last 2 hours before event date");
        }

        if (newEventDate != null && newEventDate.isBefore(minEventDateForUpdating)) {
            throw new ConditionsNotMetException("Unable to update event at last 2 hours before event date");
        }
    }

    private void updateEventState(Event event, UserStateAction action) {
        switch (action) {
            case SEND_TO_REVIEW:
                if (event.getState() == EventState.CANCELED) {
                    event.setState(EventState.PENDING);
                }
                break;
            case CANCEL_REVIEW:
                if (event.getState() != EventState.PENDING) {
                    throw new ConditionsNotMetException("Only events in PENDING state can be cancelled");
                }
                event.setState(EventState.CANCELED);
                break;
            default:
                throw new ConditionsNotMetException("Unknown state action: " + action);
        }
    }

    private void processAdminEventAction(Event event, AdminStateAction action) {
        switch (action) {
            case PUBLISH_EVENT:
                if (event.getState() != EventState.PENDING) {
                    throw new ConditionsNotMetException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                validateEventDate(event.getEventDate(), 1);
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
                break;
            case REJECT_EVENT:
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConditionsNotMetException("Cannot reject published event");
                }
                event.setState(EventState.CANCELED);
                break;
            default:
                throw new ConditionsNotMetException("Unknown state action: " + action);
        }
    }

    private List<EventShortDto> filterAvailableEvents(List<EventShortDto> events) {
        List<Long> eventIds = events.stream()
                .map(EventShortDto::getId)
                .collect(Collectors.toList());

        Map<Long, Integer> limits = eventRepository.findParticipantLimitsByIdIn(eventIds);
        Map<Long, Long> confirmedCounts = getConfirmedRequestsCounts(events);

        return events.stream()
                .filter(event -> {
                    Integer limit = limits.getOrDefault(event.getId(), 0);
                    if (limit == 0) {
                        return true;
                    }
                    Long confirmed = confirmedCounts.getOrDefault(event.getId(), 0L);
                    return confirmed < limit;
                })
                .collect(Collectors.toList());
    }

    private void enrichEvents(List<? extends Enrichable> events) {
        if (events.isEmpty()) {
            return;
        }
        enrichEventsWithConfirmedRequests(events);
        enrichEventsWithRating(events);
        enrichEventsListWithCommentsCount(events);
        enrichEventsWithUsers(events);
    }

    private void enrichEvent(EventFullDto event) {
        enrichEvents(List.of(event));
    }

    private Map<Long, Long> getConfirmedRequestsCounts(List<? extends Enrichable> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> eventIds = events.stream()
                .map(Enrichable::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return requestClient.getConfirmedRequestsCounts(eventIds);
    }

    private EventFullDto buildEventFullDto(Event event) {
        double rating = getEventRating(event.getId());
        Long confirmedRequests = getConfirmedRequestsCount(event);
        UserShortDto initiator = getUserById(event.getInitiatorId());

        EventFullDto eventFullDto = EventMapper.toEventFullDto(event, confirmedRequests, rating, initiator);
        enrichEventsListWithCommentsCount(List.of(eventFullDto));

        return eventFullDto;
    }

    private Long getConfirmedRequestsCount(Event event) {
        List<EventShortDto> list = List.of(EventShortDto.builder().id(event.getId()).build());
        return getConfirmedRequestsCounts(list).get(event.getId());
    }
}