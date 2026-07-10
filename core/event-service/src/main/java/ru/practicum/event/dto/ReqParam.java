package ru.practicum.event.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.event.model.EventSort;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Параметры поиска событий для публичного API.
 * <p>Инкапсулируют фильтры, передаваемые в {@code GET /events}.</p>
 *
 * <ul>
 *   <li>{@code text} — текст для полнотекстового поиска по аннотации/описанию;</li>
 *   <li>{@code categories} — список идентификаторов категорий для фильтрации;</li>
 *   <li>{@code paid} — признак платного/бесплатного события;</li>
 *   <li>{@code rangeStart} — дата/время начала диапазона поиска (включительно);</li>
 *   <li>{@code rangeEnd} — дата/время конца диапазона поиска (включительно);</li>
 *   <li>{@code onlyAvailable} — если {@code true}, возвращаются только события с доступными местами;</li>
 *   <li>{@code sort} — порядок сортировки ({@link ru.practicum.event.model.EventSort});</li>
 *   <li>{@code from} — смещение (offset) для постраничной выборки;</li>
 *   <li>{@code size} — размер страницы.</li>
 * </ul>
 *
 * <p>Используется в {@link ru.practicum.event.controller.PublicEventController}.</p>
 */
@Builder(toBuilder = true)
@Getter
@Setter
public class ReqParam {
    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private EventSort sort;
    private int from;
    private int size;
}
