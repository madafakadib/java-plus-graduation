package ru.practicum.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Параметры фильтрации событий для административного поиска.
 * <p>Используется в {@code AdminEventController} при запросах {@code GET /admin/events}.</p>
 *
 * <ul>
 *   <li>{@code users} — список идентификаторов инициаторов;</li>
 *   <li>{@code states} — список состояний событий (строковое представление {@link ru.practicum.dto.EventState});</li>
 *   <li>{@code categories} — список идентификаторов категорий;</li>
 *   <li>{@code rangeStart} — дата/время начала периода (включительно);</li>
 *   <li>{@code rangeEnd} — дата/время конца периода (включительно);</li>
 *   <li>{@code from} — смещение (offset) для постраничной выборки;</li>
 *   <li>{@code size} — размер страницы.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminEventParams {
    private List<Long> users;
    private List<String> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private int from;
    private int size;
}
