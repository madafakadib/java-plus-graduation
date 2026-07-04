package ru.practicum.event_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.client.*;
import ru.practicum.common.commentDto.dto.CommentCountDto;
import ru.practicum.common.commentDto.enums.CommentStatus;
import ru.practicum.common.eventDto.dto.*;
import ru.practicum.common.eventDto.dto.enums.EventSort;
import ru.practicum.common.eventDto.dto.enums.EventState;
import ru.practicum.common.eventDto.dto.paramDto.AdminUserEventParam;
import ru.practicum.common.eventDto.dto.paramDto.EventRepositoryParam;
import ru.practicum.common.eventDto.dto.paramDto.PublicUserEventParam;
import ru.practicum.common.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.common.requestDto.dto.EventRequestStatusUpdateRequest;
import ru.practicum.common.requestDto.dto.EventRequestStatusUpdateResult;
import ru.practicum.common.requestDto.dto.ParticipationRequestDto;
import ru.practicum.event_service.mapper.EventMapper;
import ru.practicum.event_service.model.Event;
import ru.practicum.event_service.repository.EventRepository;
import ru.practicum.stat.client.StatsClient;
import ru.practicum.stat.dto.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final CategoryClient categoryClient;
    private final ParticipationRequestClient participationRequestClient;
    private final CommentClient commentClient;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        LocalDateTime minEventDate = LocalDateTime.now().plusHours(2);
        if (newEventDto.getEventDate().isBefore(minEventDate)) {
            throw new ConditionsNotMetException("Event date must be at least 2 hours from now");
        }

        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        var category = categoryClient.getCategoryById(newEventDto.getCategory());
        if (category == null) {
            throw new NotFoundException("Category with id=" + newEventDto.getCategory() + " was not found");
        }

        Event event = Event.builder()
                .annotation(newEventDto.getAnnotation())
                .categoryId(newEventDto.getCategory())
                .description(newEventDto.getDescription())
                .eventDate(newEventDto.getEventDate())
                .location(newEventDto.getLocation())
                .paid(newEventDto.getPaid() != null ? newEventDto.getPaid() : false)
                .participantLimit(newEventDto.getParticipantLimit() != null ? newEventDto.getParticipantLimit() : 0)
                .requestModeration(newEventDto.getRequestModeration() != null ? newEventDto.getRequestModeration() : true)
                .title(newEventDto.getTitle())
                .initiatorId(userId)
                .state(EventState.PENDING)
                .createdOn(LocalDateTime.now())
                .build();

        event = eventRepository.save(event);

        var initiator = userClient.getUserShort(userId);

        return EventMapper.toEventFullDto(event, 0L, 0L, category, initiator);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        EventRepositoryParam param = EventRepositoryParam.builder()
                .users(List.of(userId))
                .from(from)
                .size(size)
                .build();

        List<EventShortDto> events = eventRepository.findEventsShortDto(param);
        if (events.isEmpty()) {
            return events;
        }

        enrichEventsWithViews(events);
        enrichEventsListWithCommentsCount(events);

        return events;
    }

    @Override
    public List<EventShortDto> getEventsForPublicRequests(PublicUserEventParam userEventParam) {
        EventRepositoryParam param = EventRepositoryParam.fromUserEventParam(userEventParam);

        List<EventShortDto> events = eventRepository.findEventsShortDto(param);
        if (events.isEmpty()) {
            return events;
        }

        enrichEventsWithViews(events);
        enrichEventsListWithCommentsCount(events);

        if (param.getSortOrDefault() == EventSort.VIEWS) {
            events.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        sendHit(userEventParam.getUri(), userEventParam.getIp(), LocalDateTime.now());

        return events;
    }

    @Override
    public List<EventFullDto> getEventsForAdminRequests(AdminUserEventParam adminParam) {
        EventRepositoryParam param = EventRepositoryParam.fromAdminEventParam(adminParam);

        List<EventFullDto> events = eventRepository.findEventsFullDto(param);
        if (events.isEmpty()) {
            return events;
        }

        enrichEventsWithViews(events);
        enrichEventsListWithCommentsCount(events);

        return events;
    }

    @Override
    public EventFullDto findUserEventByEventId(Long userId, Long eventId) {
        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        EventFullDto event = eventRepository.findEventByIdFullDto(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        enrichEventWithViews(event);
        enrichEventsListWithCommentsCount(List.of(event));

        return event;
    }

    @Override
    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest body) {
        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsNotMetException("Only events with CANCELED or PENDING state can be updated");
        }

        LocalDateTime minEventDateForUpdating = LocalDateTime.now().plusHours(2);
        if (event.getEventDate().isBefore(minEventDateForUpdating)) {
            throw new ConditionsNotMetException("Unable to update event at last 2 hours before event date");
        }

        if (body.getEventDate() != null && body.getEventDate().isBefore(minEventDateForUpdating)) {
            throw new ConditionsNotMetException("Unable to update event at last 2 hours before event date");
        }

        if (body.getStateAction() != null) {
            switch (body.getStateAction()) {
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
                    throw new ConditionsNotMetException("Unknown state action: " + body.getStateAction());
            }
        }

        Long categoryId = null;
        if (body.getCategory() != null) {
            var category = categoryClient.getCategoryById(body.getCategory());
            if (category == null) {
                throw new NotFoundException("Category with id=" + body.getCategory() + " was not found");
            }
            categoryId = body.getCategory();
        }

        EventMapper.updateEventFromUserRequest(body, event, categoryId);

        event = eventRepository.save(event);

        Long views = getEventViews(event.getId(), event.getEventDate());
        long confirmedRequests = participationRequestClient.countConfirmedRequests(eventId);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(
                event,
                confirmedRequests,
                views,
                categoryClient.getCategoryById(event.getCategoryId()),
                userClient.getUserShort(event.getInitiatorId())
        );
        enrichEventsListWithCommentsCount(List.of(eventFullDto));

        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest body) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Long categoryId = null;
        if (body.getCategory() != null) {
            var category = categoryClient.getCategoryById(body.getCategory());
            if (category == null) {
                throw new NotFoundException("Category with id=" + body.getCategory() + " was not found");
            }
            categoryId = body.getCategory();
        }

        EventMapper.updateEventFromAdminRequest(body, event, categoryId);

        if (body.getStateAction() != null) {
            switch (body.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != EventState.PENDING) {
                        throw new ConditionsNotMetException("Cannot publish the event because it's not in the right state: " + event.getState());
                    }
                    LocalDateTime minPublishDate = LocalDateTime.now().plusHours(1);
                    if (event.getEventDate().isBefore(minPublishDate)) {
                        throw new ConditionsNotMetException("Event date must be at least 1 hour from now");
                    }
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
                    throw new ConditionsNotMetException("Unknown state action: " + body.getStateAction());
            }
        }

        event = eventRepository.save(event);

        Long views = getEventViews(event.getId(), event.getEventDate());
        long confirmedRequests = participationRequestClient.countConfirmedRequests(eventId);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(
                event,
                confirmedRequests,
                views,
                categoryClient.getCategoryById(event.getCategoryId()),
                userClient.getUserShort(event.getInitiatorId())
        );
        enrichEventsListWithCommentsCount(List.of(eventFullDto));

        return eventFullDto;
    }

    @Override
    public EventFullDto findEventById(String uri, String ip, Long id) {
        EventFullDto event = eventRepository.findEventByIdFullDto(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Published Event with id=" + id + " was not found");
        }

        sendHit(uri, ip, LocalDateTime.now());
        enrichEventWithViews(event);
        enrichEventsListWithCommentsCount(List.of(event));

        return event;
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequests(Long userId, Long eventId) {
        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        return participationRequestClient.getRequestsByEventId(eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatuses(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found for user with id=" + userId);
        }

        return participationRequestClient.updateRequestStatuses(eventId, updateRequest);
    }

    @Override
    public boolean existsByCategoryId(Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    @Override
    public List<EventShortDto> getShortDtosByIds(Collection<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findAllByIdIn(eventIds);

        List<EventShortDto> dtos = events.stream()
                .map(event -> {
                    var category = categoryClient.getCategoryById(event.getCategoryId());
                    var initiator = userClient.getUserShort(event.getInitiatorId());
                    return EventMapper.toEventShortDto(event, 0L, 0L, category, initiator);
                })
                .collect(Collectors.toList());

        enrichEventsWithViews(dtos);
        enrichEventsListWithCommentsCount(dtos);

        return dtos;
    }

    private Long getEventViews(Long eventId, LocalDateTime publishedOn) {
        String[] uris = {"/events/" + eventId};
        Map<Long, Long> hits = fetchViews(uris, publishedOn);
        return hits.getOrDefault(eventId, 0L);
    }

    private <T extends Viewable> void enrichEventsWithViews(List<T> events) {
        LocalDateTime minEventDate = events.stream()
                .map(Viewable::getPublishedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        String[] uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toArray(String[]::new);

        Map<Long, Long> hits = fetchViews(uris, minEventDate);

        events.forEach(event ->
                event.setViews(hits.getOrDefault(event.getId(), 0L))
        );
    }

    private Map<Long, Long> fetchViews(String[] uris, LocalDateTime date) {
        ParamDto statRequestParam = ParamDto.builder()
                .start(date)
                .end(LocalDateTime.now().plusSeconds(1))
                .uris(uris)
                .unique(true)
                .build();

        log.debug("Fetching views for uris: {}, params: {}", Arrays.toString(uris), statRequestParam);

        try {
            List<ViewStatsDto> stats = statsClient.get(statRequestParam);

            if (stats.size() == 1 && stats.getFirst().getHits() == -1) {
                log.error("Failed to fetch views from stats-service, returned hits = -1 (Fail marker)");
                return Collections.emptyMap();
            }

            return stats.stream()
                    .filter(stat -> stat.getUri() != null && stat.getHits() != -1)
                    .collect(Collectors.toMap(
                            this::extractEventIdFromUri,
                            ViewStatsDto::getHits
                    ));
        } catch (Exception e) {
            log.error("Failed to fetch views from stats-service", e);
            return Collections.emptyMap();
        }
    }

    private void enrichEventWithViews(EventFullDto event) {
        String[] uris = {"/events/" + event.getId()};
        Map<Long, Long> hits = fetchViews(uris, event.getPublishedOn());
        event.setViews(hits.getOrDefault(event.getId(), 0L));
    }

    private Long extractEventIdFromUri(ViewStatsDto stat) {
        String uri = stat.getUri();
        return Long.parseLong(uri.substring(uri.lastIndexOf('/') + 1));
    }

    private void sendHit(String uri, String ip, LocalDateTime time) {
        EndpointHitDto hitDto = EndpointHitDto.builder()
                .uri(uri)
                .ip(ip)
                .timestamp(time)
                .build();

        statsClient.hit(hitDto);
    }

    private <T extends Commentable> void enrichEventsListWithCommentsCount(List<T> eventDtos) {
        if (eventDtos.isEmpty()) return;

        List<Long> ids = eventDtos.stream()
                .map(Commentable::getId)
                .collect(Collectors.toList());

        List<CommentCountDto> counts = commentClient.countByEventIdInAndStatus(ids, CommentStatus.APPROVED);

        Map<Long, Long> countsMap = counts.stream()
                .collect(Collectors.toMap(
                        CommentCountDto::getEventId,
                        CommentCountDto::getCount
                ));

        eventDtos.forEach(item -> item.setCommentsCount(countsMap.getOrDefault(item.getId(), 0L)));
    }
}