package ru.practicum.eventsService.event.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.common.dto.events.EventFullDto;
import ru.practicum.common.dto.events.EventShortDto;
import ru.practicum.common.dto.events.EventState;
import ru.practicum.common.dto.events.Location;
import ru.practicum.common.dto.events.category.CategoryDto;
import ru.practicum.common.dto.users.UserShortDto;
import ru.practicum.eventsService.event.dto.paramDto.EventRepositoryParam;
import ru.practicum.eventsService.event.model.QEvent;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private final QEvent event = QEvent.event;


    @Override
    public List<EventShortDto> findEventsShortDto(EventRepositoryParam param) {

        BooleanBuilder predicate = createPredicate(param);

        return queryFactory
                .select(Projections.constructor(EventShortDto.class,
                        event.id,
                        event.annotation,
                        Projections.constructor(CategoryDto.class,
                                event.category.id,
                                event.category.name
                        ),
                        Expressions.asNumber(0L).as("confirmedRequests"),
                        event.eventDate,
                        Projections.constructor(UserShortDto.class,
                                event.initiatorId,
                                Expressions.nullExpression(String.class)
                        ),
                        event.paid,
                        event.title,
                        Expressions.asNumber(0L).as("commentsCount"),
                        Expressions.asNumber(0.0).as("rating"),
                        Expressions.asNumber(0L).as("views")
                ))
                .from(event)
                .where(predicate)
                .orderBy(event.eventDate.asc())
                .offset(param.getFrom())
                .limit(param.getSize())
                .fetch();
    }


    @Override
    public Optional<EventFullDto> findEventByIdFullDto(Long id) {
        EventFullDto result = queryFactory
                .select(Projections.constructor(EventFullDto.class,
                        event.id,
                        event.annotation,
                        Projections.constructor(CategoryDto.class,
                                event.category.id,
                                event.category.name
                        ),
                        Expressions.asNumber(0L).as("confirmedRequests"),
                        event.createdOn,
                        event.description,
                        event.eventDate,
                        Projections.constructor(UserShortDto.class,
                                event.initiatorId,
                                Expressions.nullExpression(String.class)
                        ),
                        Projections.constructor(Location.class,
                                event.location.lat,
                                event.location.lon
                        ),
                        event.paid,
                        event.participantLimit,
                        event.publishedOn,
                        event.requestModeration,
                        event.state,
                        event.title,
                        Expressions.asNumber(0.0).as("rating"),
                        Expressions.asNumber(0L).as("commentsCount"),
                        Expressions.asNumber(0L).as("views")
                ))
                .from(event)
                .where(event.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }


    @Override
    public List<EventFullDto> findEventsFullDto(EventRepositoryParam param) {
        BooleanBuilder predicate = createPredicate(param);

        return queryFactory
                .select(Projections.constructor(EventFullDto.class,
                        event.id,
                        event.annotation,
                        Projections.constructor(CategoryDto.class,
                                event.category.id,
                                event.category.name
                        ),
                        Expressions.asNumber(0L).as("confirmedRequests"),
                        event.createdOn,
                        event.description,
                        event.eventDate,
                        Projections.constructor(UserShortDto.class,
                                event.initiatorId,
                                Expressions.nullExpression(String.class)
                        ),
                        Projections.constructor(Location.class,
                                event.location.lat,
                                event.location.lon
                        ),
                        event.paid,
                        event.participantLimit,
                        event.publishedOn,
                        event.requestModeration,
                        event.state,
                        event.title,
                        Expressions.asNumber(0.0).as("rating"),
                        Expressions.asNumber(0L).as("commentsCount"),
                        Expressions.asNumber(0L).as("views")
                ))
                .from(event)
                .where(predicate)
                .orderBy(event.eventDate.asc())
                .offset(param.getFrom())
                .limit(param.getSize())
                .fetch();
    }

    private BooleanBuilder createPredicate(EventRepositoryParam param) {
        BooleanBuilder predicate = new BooleanBuilder();

        if (param.isPublicRequest()) {
            predicate.and(event.state.eq(EventState.PUBLISHED));
        } else if (param.hasStates()) {
            predicate.and(event.state.in(param.getStates()));
        }

        if (param.hasTextSearchRequest()) {
            predicate.and(
                    event.annotation.containsIgnoreCase(param.getText())
                            .or(event.description.containsIgnoreCase(param.getText())));
        }

        if (param.hasCategories()) {
            predicate.and(event.category.id.in(param.getCategories()));
        }

        if (param.hasPaidParam()) {
            predicate.and(event.paid.eq(param.getPaid()));
        }

        if (param.hasUsers()) {
            predicate.and(event.initiatorId.in(param.getUsers()));
        }

        if (param.hasIds()) {
            predicate.and(event.id.in(param.getIds()));
        }

        if (param.hasDateRange()) {
            predicate.and(event.eventDate.between(param.getRangeStart(), param.getRangeEnd()));
        } else if (param.hasRangeStart()) {
            predicate.and(event.eventDate.after(param.getRangeStart()));
        } else if (param.hasRangeEnd()) {
            predicate.and(event.eventDate.before(param.getRangeEnd()));
        } else {
            predicate.and(event.eventDate.after(LocalDateTime.now()));
        }

        return predicate;
    }

    @Override
    public Map<Long, Integer> findParticipantLimitsByIdIn(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return queryFactory
                .select(event.id, event.participantLimit)
                .from(event)
                .where(event.id.in(eventIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.get(event.id),
                        tuple -> tuple.get(event.participantLimit)
                ));
    }
}