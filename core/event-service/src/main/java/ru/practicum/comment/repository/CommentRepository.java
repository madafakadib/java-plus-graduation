package ru.practicum.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.dto.EventCommentCount;

import java.util.List;
/**
 * Spring Data JPA-репозиторий для сущностей {@link ru.practicum.comment.model.Comment}.
 * <p>Предоставляет стандартные CRUD-операции и специализированные методы выборки.</p>
 *
 * <ul>
 *   <li>{@link #findAllByEventId(Long, org.springframework.data.domain.Pageable)} —
 *       комментарии по событию с пагинацией;</li>
 *   <li>{@link #findAllByEventIdAndAuthorId(Long, Long, org.springframework.data.domain.Pageable)} —
 *       комментарии пользователя для конкретного события;</li>
 *   <li>{@link #findAllByAuthorId(Long, org.springframework.data.domain.Pageable)} —
 *       все комментарии пользователя;</li>
 *   <li>{@link #countCommentByEvent_Id(Long)} — количество комментариев к событию;</li>
 *   <li>{@link #findAllByEventIds(List)} — агрегированное количество комментариев по списку событий
 *       (через нативный SQL-запрос, возвращает {@link ru.practicum.event.dto.EventCommentCount}).</li>
 * </ul>
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEventId(Long eventId, Pageable pageable);

    List<Comment> findAllByEventIdAndAuthorId(Long eventId, Long authorId, Pageable pageable);

    List<Comment> findAllByAuthorId(Long authorId, Pageable pageable);

    long countCommentByEvent_Id(Long eventId);

    @Query(value = """
            SELECT c.event_id AS eventId, count(event_id) AS commentCount
                   FROM comments c
                   WHERE c.event_id IN :eventsIds
                        GROUP BY c.event_id
            """, nativeQuery = true)
    List<EventCommentCount> findAllByEventIds(@Param("eventsIds") List<Long> eventsIds);
}