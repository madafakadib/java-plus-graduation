package ru.practicum.event.dto;
/**
 * Проекция для выборки количества комментариев к событиям.
 * <p>Используется в {@link ru.practicum.comment.repository.CommentRepository}
 * при выполнении нативного запроса с группировкой по {@code event_id}.</p>
 *
 * <ul>
 *   <li>{@link #getEventId()} — идентификатор события;</li>
 *   <li>{@link #getCommentCount()} — количество комментариев для события.</li>
 * </ul>
 */
public interface EventCommentCount {

    Long getEventId();

    Long getCommentCount();

}
