package ru.practicum.event_service.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.common.categoryDto.dto.CategoryDto;
import ru.practicum.common.eventDto.dto.EventFullDto;
import ru.practicum.common.eventDto.dto.EventShortDto;
import ru.practicum.common.eventDto.dto.enums.EventState;
import ru.practicum.common.eventDto.dto.paramDto.EventRepositoryParam;
import ru.practicum.common.userDto.dto.UserShortDto;
import ru.practicum.event_service.model.QEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QEvent event = QEvent.event;

    @Override
    public List<EventShortDto> findEventsShortDto(EventRepositoryParam param) {
        BooleanBuilder predicate = createPredicate(param);

        var query = queryFactory
                .select(Projections.constructor(EventShortDto.class,
                        event.id,
                        event.annotation,
                        Projections.constructor(CategoryDto.class,
                                Expressions.asNumber(0L).as("id"),
                                Expressions.asString("").as("name")
                        ),
                        Expressions.asNumber(0L).as("confirmedRequests"),
                        event.eventDate,
                        Projections.constructor(UserShortDto.class,
                                Expressions.asNumber(0L).as("id"),
                                Expressions.asString("").as("name")
                        ),
                        event.paid,
                        event.title,
                        Expressions.asNumber(0L).as("views"),
                        Expressions.asNumber(0L).as("commentsCount")
                ))
                .from(event)
                .where(predicate)
                .orderBy(event.eventDate.asc())
                .offset(param.getFrom())
                .limit(param.getSize())
                .fetch();

        return query;
    }

    @Override
    public Optional<EventFullDto> findEventByIdFullDto(Long id) {
        EventFullDto result = queryFactory
                .select(Projections.constructor(EventFullDto.class,
                        event.id,
                        event.annotation,
                        Projections.constructor(CategoryDto.class,
                                Expressions.asNumber(0L).as("id"),
                                Expressions.asString("").as("name")
                        ),
                        Expressions.asNumber(0L).as("confirmedRequests"),
                        event.createdOn,
                        event.description,
                        event.eventDate,
                        Projections.constructor(UserShortDto.class,
                                Expressions.asNumber(0L).as("id"),
                                Expressions.asString("").as("name")
                        ),
                        event.location,
                        event.paid,
                        event.participantLimit,
                        event.publishedOn,
                        event.requestModeration,
                        event.state,
                        event.title,
                        Expressions.asNumber(0L).as("views"),
                        Expressions.asNumber(0L).as("commentsCount")
                ))
                .from(event)
                .where(event.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<EventFullDto> findEventsFullDto(EventRepositoryParam param) {
        BooleanBuilder predicate = createPredicate(param);

        var query = queryFactory
                .select(Projections.constructor(EventFullDto.class,
                        event.id,
                        event.annotation,
                        Projections.constructor(CategoryDto.class,
                                Expressions.asNumber(0L).as("id"),
                                Expressions.asString("").as("name")
                        ),
                        Expressions.asNumber(0L).as("confirmedRequests"),
                        event.createdOn,
                        event.description,
                        event.eventDate,
                        Projections.constructor(UserShortDto.class,
                                Expressions.asNumber(0L).as("id"),
                                Expressions.asString("").as("name")
                        ),
                        event.location,
                        event.paid,
                        event.participantLimit,
                        event.publishedOn,
                        event.requestModeration,
                        event.state,
                        event.title,
                        Expressions.asNumber(0L).as("views"),
                        Expressions.asNumber(0L).as("commentsCount")
                ))
                .from(event)
                .where(predicate)
                .orderBy(event.eventDate.asc())
                .offset(param.getFrom())
                .limit(param.getSize())
                .fetch();

        return query;
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
                            .or(event.description.containsIgnoreCase(param.getText()))
            );
        }

        if (param.hasCategories()) {
            predicate.and(event.categoryId.in(param.getCategories()));
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
}