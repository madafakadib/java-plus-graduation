package ru.practicum.event.service;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ru.practicum.client.AnalyzerClient;
import ru.practicum.client.CollectorClient;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.dto.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.*;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.ewm.stats.messages.RecommendedEventProto;
import ru.practicum.exception.ConditionNotMetException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.exception.InitiatorRequestException;
import ru.practicum.exception.ValidationException;
import ru.practicum.feign.CategoryFeign;
import ru.practicum.feign.RequestFeign;
import ru.practicum.feign.UserFeign;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.dto.Constants.FORMAT_DATETIME;

/**
 * Реализация {@link EventService}.
 * <p>Публичные, приватные и админские операции над событиями: поиск, создание,
 * обновление, лайки, рекомендации. Обогащает DTO категориями, инициаторами,
 * количеством подтверждённых заявок, рейтингом и числом комментариев.</p>
 *
 * <p>Зависимости:</p>
 * <ul>
 *   <li>{@link EventRepository}, {@link LocationRepository} — доступ к БД;</li>
 *   <li>{@link EventMapper} — преобразование сущностей ↔ DTO;</li>
 *   <li>{@link UserFeign}, {@link CategoryFeign}, {@link RequestFeign} — межсервисные запросы;</li>
 *   <li>{@link CommentRepository} — агрегаты по комментариям;</li>
 *   <li>{@link AnalyzerClient}, {@link CollectorClient} — рейтинг/статистика, лайки/просмотры.</li>
 * </ul>
 *
 * <p>Исключения: {@link EntityNotFoundException}, {@link ValidationException},
 * {@link ConditionNotMetException}, {@link InitiatorRequestException}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserFeign userFeign;
    private final CategoryFeign categoryFeign;
    private final LocationRepository locationRepository;
    private final RequestFeign requestFeign;
    private final CommentRepository commentRepository;
    private final AnalyzerClient analyzerClient;
    private final CollectorClient collectorClient;

    @Override
    public List<EventShortDto> getAllEvents(ReqParam reqParam) {
        Pageable pageable = PageRequest.of(reqParam.getFrom(), reqParam.getSize());

        if (reqParam.getRangeStart() == null || reqParam.getRangeEnd() == null) {
            reqParam.setRangeStart(LocalDateTime.now());
            reqParam.setRangeEnd(LocalDateTime.now().plusYears(1));
        }

        List<Event> events = eventRepository.findEvents(
                reqParam.getText(),
                reqParam.getCategories(),
                reqParam.getPaid(),
                reqParam.getRangeStart(),
                reqParam.getRangeEnd(),
                reqParam.getOnlyAvailable(),
                pageable
        );
        log.info("Найденные события: {}", events);
        if (events.isEmpty()) {
            throw new ValidationException(ReqParam.class, " События не найдены");
        }

        List<EventFullDto> eventFullDtos = addCategoriesDto(eventMapper.toEventFullDtos(events), events);
        addUserShortDto(eventFullDtos, events);

        List<Long> eventsIds = eventFullDtos.stream().map(EventFullDto::getId).toList();
        List<EventCommentCount> eventCommentCountList = commentRepository.findAllByEventIds(eventsIds);

        eventFullDtos.forEach(eventFullDto ->
                eventFullDto.setCommentsCount(eventCommentCountList.stream()
                        .filter(eventComment -> eventComment.getEventId().equals(eventFullDto.getId()))
                        .map(EventCommentCount::getCommentCount)
                        .findFirst()
                        .orElse(0L)
                )
        );

        List<EventShortDto> addedViewsAndRequests = eventMapper.toEventShortDtos(addRequests(addRating(eventFullDtos)));

        if (reqParam.getSort() != null) {
            return switch (reqParam.getSort()) {
                case EVENT_DATE ->
                        addedViewsAndRequests.stream().sorted(Comparator.comparing(EventShortDto::getEventDate)).toList();
                case VIEWS ->
                        addedViewsAndRequests.stream().sorted(Comparator.comparing(EventShortDto::getRating)).toList();
            };
        }
        return addedViewsAndRequests;
    }

    @Override
    public List<EventFullDto> getAllEvents(AdminEventParams params) {
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize());

        if (params.getRangeStart() == null || params.getRangeEnd() == null) {
            params.setRangeStart(LocalDateTime.now());
            params.setRangeEnd(LocalDateTime.now().plusYears(1));
        }

        List<Event> events = eventRepository.findAdminEvents(
                params.getUsers(),
                params.getStates(),
                params.getCategories(),
                params.getRangeStart(),
                params.getRangeEnd(),
                pageable);
        log.info("Найденные события: {}", events);

        List<EventFullDto> eventFullDtos = addCategoriesDto(eventMapper.toEventFullDtos(events), events);
        addUserShortDto(eventFullDtos, events);
        return addRequests(addRating(eventFullDtos));
    }

    @Override
    public EventFullDto publicGetEvent(long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие c ID - " + id + ", не найдено."));
        if (event.getState() != EventState.PUBLISHED) {
            throw new EntityNotFoundException(Event.class, " Событие c ID - " + id + ", ещё не опубликовано.");
        }

        CategoryDto category = getCategoryDto(event.getCategoryId());
        UserShortDto userShortDto = getUserShortDto(event.getInitiatorId());

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event, userShortDto, category);
        eventFullDto.setCommentsCount(commentRepository.countCommentByEvent_Id(event.getId()));
        return addRequests(addRating(eventFullDto));
    }

    @Override
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        LocalDateTime eventDate = LocalDateTime.parse(newEventDto.getEventDate(),
                DateTimeFormatter.ofPattern(FORMAT_DATETIME));
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(NewEventDto.class, "До начала события осталось меньше двух часов");
        }

        UserShortDto initiator = getUserShortDto(userId);
        CategoryDto category = getCategoryDto(newEventDto.getCategory());

        Event event = eventMapper.toEvent(newEventDto);
        log.info("Создаем событие: {}", event);
        if (newEventDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        if (newEventDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0L);
        }
        event.setInitiatorId(initiator.getId());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event.setLocation(locationRepository.save(event.getLocation()));

        event = eventRepository.save(event);
        log.info("Сохраняем новое событиев БД: {}", event);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event, initiator, category);
        log.info("Результат маппинга: {}", eventFullDto);
        return eventFullDto;
    }

    @Override
    public EventFullDto update(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, " c ID = " + eventId + ", не найдено."));

        if (updateEventAdminRequest.getEventDate() != null) {
            if ((event.getPublishedOn() != null) && updateEventAdminRequest.getEventDate().isAfter(event.getPublishedOn().minusHours(1))) {
                throw new ConditionNotMetException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
        }
        if (updateEventAdminRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT && event.getState() != EventState.PENDING) {
            throw new ConditionNotMetException("Cобытие можно публиковать, только если оно в состоянии ожидания публикации");
        }
        if (updateEventAdminRequest.getStateAction() == AdminStateAction.REJECT_EVENT && event.getState() == EventState.PUBLISHED) {
            throw new ConditionNotMetException("Cобытие можно отклонить, только если оно еще не опубликовано");
        }
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }
            if (updateEventAdminRequest.getStateAction() == AdminStateAction.REJECT_EVENT) {
                event.setState(EventState.CANCELED);
            }
        }

        checkEvent(event, updateEventAdminRequest);

        CategoryDto category = getCategoryDto(event.getCategoryId());
        UserShortDto initiator = getUserShortDto(event.getInitiatorId());
        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event, initiator, category);
    }

    @Override
    public List<EventShortDto> findUserEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        List<EventFullDto> eventFullDtos = addCategoriesDto(eventMapper.toEventFullDtos(events), events);
        addUserShortDto(eventFullDtos, events);
        return eventMapper.toEventShortDtos(addRequests(addRating(eventFullDtos)));
    }

    @Override
    public EventFullDto findUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие не найдено"));
        CategoryDto category = getCategoryDto(event.getCategoryId());
        UserShortDto user = getUserShortDto(userId);
        EventFullDto result = eventMapper.toEventFullDto(event, user, category);
        return addRequests(addRating(result));
    }

    @Override
    public Optional<EventFullDto> findOptEventByUserIdAndId(Long userId, Long eventId) {
        Optional<Event> eventOpt = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (eventOpt.isEmpty()) {
            return Optional.empty();
        }
        Event event = eventOpt.get();
        CategoryDto category = getCategoryDto(event.getCategoryId());
        UserShortDto user = getUserShortDto(userId);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event, user, category);
        addRequests(addRating(eventFullDto));
        return Optional.of(eventFullDto);
    }

    @Override
    public EventFullDto findEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие c ID - " + eventId + ", не найдено."));

        CategoryDto category = getCategoryDto(event.getCategoryId());
        UserShortDto userShortDto = getUserShortDto(event.getInitiatorId());

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event, userShortDto, category);
        eventFullDto.setCommentsCount(commentRepository.countCommentByEvent_Id(event.getId()));
        return addRequests(addRating(eventFullDto));
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "Событие не найдено"));
        if (event.getState() == EventState.PUBLISHED) {
            throw new InitiatorRequestException("Нельзя отредактировать опубликованное событие");
        }

        if (updateRequest.getEventDate() != null) {
            if (updateRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(NewEventDto.class, "До начала события осталось меньше двух часов");
            }
        }
        if (updateRequest.getStateAction() != null) {
            if (updateRequest.getStateAction() == PrivateStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
            if (updateRequest.getStateAction() == PrivateStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            }
        }

        checkEvent(event, updateRequest);
        CategoryDto category = getCategoryDto(event.getCategoryId());
        UserShortDto user = getUserShortDto(userId);

        event = eventRepository.save(event);
        return eventMapper.toEventFullDto(event, user, category);
    }

    @Override
    public List<EventFullDto> findAllByCategoryId(Long categoryId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);

        List<Event> events = eventRepository.findAllByCategoryId(categoryId, pageable);
        log.info("По категории с id: {}, найдены события: {}", categoryId, events);
        return eventMapper.toEventFullDtos(events);
    }

    @Override
    public void likeEvent(Long userId, Long eventId) {
        try {
            ParticipationRequestDto request = requestFeign.findByRequesterIdAndEventId(userId, eventId)
                    .orElseThrow(() -> new EntityNotFoundException(ParticipationRequestDto.class, "Запрос не найден"));
            if (!RequestStatus.CONFIRMED.name().equals(request.getStatus())) {
                throw new ValidationException(ParticipationRequestDto.class, "Пользователь может лайкать только посещённые мероприятия");
            }
            collectorClient.sendLike(userId, eventId);
        } catch (FeignException e) {
            throw new EntityNotFoundException(ParticipationRequestDto.class, "Ошибка при обращении в request-service");
        }
    }

    @Override
    public List<EventFullDto> getRecommendations(Long userId, Integer maxResults) {
        List<Long> ids = analyzerClient.getRecommendations(userId, maxResults).stream()
                .sorted((a, b) -> (int) (a.getScore() - b.getScore()))
                .map(RecommendedEventProto::getEventId).toList();
        List<Event> events = eventRepository.findAllById(ids);
        List<EventFullDto> eventDtos = eventMapper.toEventFullDtos(events);
        addCategoriesDto(eventDtos, events);
        addUserShortDto(eventDtos, events);
        addRequests(eventDtos);
        addRating(eventDtos);
        log.info("Рекомендации пользователю: {}", eventDtos);
        return eventDtos;
    }


    /**
     * Применяет поля из запроса обновления к сущности события (без сохранения).
     *
     * @param event         изменяемая сущность события
     * @param updateRequest базовый DTO обновления
     */
    private void checkEvent(Event event, UpdateEventBaseRequest updateRequest) {
        if (updateRequest.getAnnotation() != null && !updateRequest.getAnnotation().isBlank()) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            event.setCategoryId(updateRequest.getCategory());
        }
        if (updateRequest.getDescription() != null && !updateRequest.getDescription().isBlank()) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(updateRequest.getEventDate());
        }
        if (updateRequest.getLocation() != null) {
            Optional<Location> locationOpt = locationRepository.findByLatAndLon(
                    updateRequest.getLocation().getLat(),
                    updateRequest.getLocation().getLon());
            Location location = locationOpt.orElse(locationRepository.save(
                    new Location(null, updateRequest.getLocation().getLat(), updateRequest.getLocation().getLon())));
            event.setLocation(location);
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit().longValue());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isBlank()) {
            event.setTitle(updateRequest.getTitle());
        }
    }

    /**
     * Добавляет в список событий количество подтверждённых заявок.
     * Данные загружаются из request-service.
     *
     * @param eventDtos события в формате {@link EventFullDto}
     * @return те же DTO с заполненным {@code confirmedRequests}
     * @throws EntityNotFoundException при ошибке обращения в request-service
     */
    private List<EventFullDto> addRequests(List<EventFullDto> eventDtos) {
        List<Long> eventIds = eventDtos.stream().map(EventFullDto::getId).toList();
        List<ParticipationRequestDto> requests;
        try {
            requests = requestFeign.findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);
            log.info("Получаем запросы из request-service: {}", requests);
        } catch (FeignException e) {
            throw new EntityNotFoundException(ParticipationRequestDto.class, "Ошибка при обращении в request-service");
        }
        Map<Long, Long> requestsMap = requests.stream()
                .collect(Collectors.groupingBy(ParticipationRequestDto::getEvent, Collectors.counting()));
        eventDtos.forEach(eventDto -> eventDto.setConfirmedRequests(requestsMap.getOrDefault(eventDto.getId(), 0L)));
        return eventDtos;
    }

    /**
     * Добавляет к событию количество подтверждённых заявок.
     *
     * @param eventDto событие
     * @return то же событие с заполненным {@code confirmedRequests}
     * @throws EntityNotFoundException при ошибке обращения в request-service
     */
    private EventFullDto addRequests(EventFullDto eventDto) {
        try {
            eventDto.setConfirmedRequests(
                    requestFeign.findCountByEventIdInAndStatus(eventDto.getId(), RequestStatus.CONFIRMED)
            );
        } catch (FeignException e) {
            throw new EntityNotFoundException(ParticipationRequestDto.class, "Ошибка при обращении в request-service");
        }
        return eventDto;
    }

    /**
     * Загружает категорию по идентификатору через category-service.
     *
     * @param categoryId id категории
     * @return {@link CategoryDto}
     * @throws EntityNotFoundException если категория недоступна/не найдена
     */
    private CategoryDto getCategoryDto(Long categoryId) {
        try {
            CategoryDto category = categoryFeign.getCategoryById(categoryId);
            log.info("Получаем категорию из category-service: {}", category);
            return category;
        } catch (FeignException e) {
            throw new EntityNotFoundException(CategoryDto.class, e.getMessage());
        }
    }

    /**
     * Обогащает события категориями (map по {@code event.categoryId}).
     *
     * @param dtos   события (будут модифицированы)
     * @param events исходные сущности событий
     * @return те же DTO с заполненным {@code category}
     * @throws EntityNotFoundException при ошибке обращения в category-service
     */
    private List<EventFullDto> addCategoriesDto(List<EventFullDto> dtos, List<Event> events) {
        Map<Long, EventFullDto> dtoMap = dtos.stream().collect(Collectors.toMap(EventFullDto::getId, Function.identity()));

        Set<Long> categoriesId = events.stream().map(Event::getCategoryId).collect(Collectors.toSet());
        Map<Long, CategoryDto> categories;
        try {
            categories = categoryFeign.getCategoryById(categoriesId);
            log.info("Получаем категории из category-service: {}", categories);
        } catch (FeignException e) {
            throw new EntityNotFoundException(CategoryDto.class, e.getMessage());
        }
        for (Event event : events) {
            dtoMap.get(event.getId()).setCategory(categories.get(event.getCategoryId()));
        }
        log.info("Добавляем категории: {}", dtos);
        return dtos;
    }

    /**
     * Загружает краткую информацию о пользователе через user-service.
     *
     * @param userId id пользователя
     * @return {@link UserShortDto}
     * @throws EntityNotFoundException если пользователь недоступен/не найден
     */
    private UserShortDto getUserShortDto(Long userId) {
        try {
            UserShortDto user = userFeign.findUserShortDtoById(userId);
            log.info("Получаем пользователя из user-service: {}", user);
            return user;
        } catch (FeignException e) {
            throw new EntityNotFoundException(UserShortDto.class, e.getMessage());
        }
    }

    /**
     * Обогащает события инициаторами (map по {@code event.initiatorId}).
     *
     * @param dtos   события (будут модифицированы)
     * @param events исходные сущности событий
     * @return те же DTO с заполненным {@code initiator}
     * @throws EntityNotFoundException при ошибке обращения в user-service
     */
    private List<EventFullDto> addUserShortDto(List<EventFullDto> dtos, List<Event> events) {
        Map<Long, EventFullDto> dtoMap = dtos.stream().collect(Collectors.toMap(EventFullDto::getId, Function.identity()));

        Set<Long> usersId = events.stream().map(Event::getInitiatorId).collect(Collectors.toSet());
        Map<Long, UserShortDto> users;
        try {
            users = userFeign.findUserShortDtoById(usersId);
            log.info("Получаем пользователей из user-service: {}", users);
        } catch (FeignException e) {
            throw new EntityNotFoundException(UserShortDto.class, e.getMessage());
        }
        for (Event event : events) {
            dtoMap.get(event.getId()).setInitiator(users.get(event.getInitiatorId()));
        }
        log.info("Добавляем пользователей: {}", dtos);
        return dtos;
    }

    /**
     * Проставляет рейтинг для списка событий, запрашивая агрегаты у {@link AnalyzerClient}.
     *
     * @param events список событий
     * @return те же события с заполненным {@code rating}
     */
    private List<EventFullDto> addRating(List<EventFullDto> events) {
        Map<Long, Double> ratings = analyzerClient
                .getInteractionsCount(events.stream().map(EventFullDto::getId).collect(Collectors.toList()));
        log.info("Найденный рэйтинг для событий: {}", ratings);
        for (EventFullDto e : events) {
            e.setRating(ratings.getOrDefault(e.getId(), 0.0));
        }
        return events;
    }

    /**
     * Проставляет рейтинг для одного события.
     *
     * @param event событие
     * @return событие с заполненным {@code rating}
     */
    private EventFullDto addRating(EventFullDto event) {
        Map<Long, Double> rating = analyzerClient
                .getInteractionsCount(List.of(event.getId()));
        log.info("Найденный рэйтинг для события: {}", rating);
        event.setRating(rating.getOrDefault(event.getId(), 0.0));
        return event;
    }
}