package ru.practicum.event.model;
/**
 * Варианты сортировки событий в публичном API.
 * <p>Используется в {@link ru.practicum.event.dto.ReqParam} при запросах {@code GET /events}.</p>
 *
 * <ul>
 *   <li>{@link #EVENT_DATE} — сортировка по дате проведения события;</li>
 *   <li>{@link #VIEWS} — сортировка по количеству просмотров.</li>
 * </ul>
 */
public enum EventSort {
    EVENT_DATE,
    VIEWS
}
