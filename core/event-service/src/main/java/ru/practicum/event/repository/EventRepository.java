package ru.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.EventState;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
/**
 * Spring Data JPA-репозиторий для работы с событиями ({@link ru.practicum.event.model.Event}).
 * <p>Предоставляет стандартные CRUD-операции и набор кастомных запросов.</p>
 *
 * <ul>
 *   <li>{@link #findEvents(String, List, Boolean, LocalDateTime, LocalDateTime, Boolean, Pageable)} —
 *       публичный поиск событий с фильтрацией по тексту, категориям, платности,
 *       временному диапазону и признаку публикации;</li>
 *   <li>{@link #findAdminEvents(List, List, List, LocalDateTime, LocalDateTime, Pageable)} —
 *       административный поиск с фильтрацией по пользователям, состояниям и категориям;</li>
 *   <li>{@link #findByIdAndInitiatorId(Long, Long)} —
 *       поиск события по id и id инициатора (для приватного API);</li>
 *   <li>{@link #findAllByIdIsIn(Collection)} —
 *       выборка событий по множеству id;</li>
 *   <li>{@link #findAllByInitiatorId(Long)} и {@link #findAllByInitiatorId(Long, Pageable)} —
 *       выборка событий инициатора (все или постранично);</li>
 *   <li>{@link #findByIdAndState(Long, ru.practicum.dto.EventState)} —
 *       поиск события по id и состоянию;</li>
 *   <li>{@link #findAllByCategoryId(Long, Pageable)} —
 *       выборка событий по категории (с пагинацией).</li>
 * </ul>
 *
 * <p>Используется в сервисном слое для публичного, приватного и административного API.</p>
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
            SELECT e FROM Event e
            WHERE (?1 IS NULL OR (e.title ILIKE ?1
            OR e.description ILIKE ?1
            OR e.annotation ILIKE ?1))
            AND (?2 IS NULL OR e.categoryId IN ?2)
            AND (?3 IS NULL OR e.paid = ?3)
            AND e.eventDate BETWEEN ?4 AND ?5
            AND (?6 IS NULL OR e.state = 'PUBLISHED')
            """)
    List<Event> findEvents(String text,
                           List<Long> categories,
                           Boolean paid,
                           LocalDateTime rangeStart,
                           LocalDateTime rangeEnd,
                           Boolean onlyAvailable,
                           Pageable pageable);

    @Query("""
            SELECT e FROM Event e
            WHERE (:users IS NULL OR e.initiatorId IN :users)
            AND (:states IS NULL OR e.state IN :states)
            AND (:categories IS NULL OR e.categoryId IN :categories)
            AND e.eventDate BETWEEN :rangeStart AND :rangeEnd
            """)
    List<Event> findAdminEvents(List<Long> users,
                                List<String> states,
                                List<Long> categories,
                                LocalDateTime rangeStart,
                                LocalDateTime rangeEnd,
                                Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    List<Event> findAllByIdIsIn(Collection<Long> ids);

    List<Event> findAllByInitiatorId(Long initiatorId);

    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndState(Long eventId, EventState state);

    List<Event> findAllByCategoryId(Long categoryId, Pageable pageable);
}