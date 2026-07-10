package ru.practicum.compilation.dto;



import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
/**
 * DTO для обновления подборки событий.
 * <p>Используется в административном API при {@code PATCH /admin/compilations/{compId}}.</p>
 *
 * <ul>
 *   <li>{@code events} — новый список идентификаторов событий для включения в подборку (может быть пустым);</li>
 *   <li>{@code pinned} — признак закреплённой подборки на главной странице
 *       (по умолчанию {@code false}, может быть изменён);</li>
 *   <li>{@code title} — новое название подборки, длиной от 1 до 50 символов (необязательное).</li>
 * </ul>
 */
@Data
public class UpdateCompilationRequest {

    @Nullable
    private List<Long> events;

    private Boolean pinned = false;

    @Size(min = 1, max = 50)
    private String title;

}