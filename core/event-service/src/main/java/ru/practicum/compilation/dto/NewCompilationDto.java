package ru.practicum.compilation.dto;


import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
/**
 * DTO для создания новой подборки событий.
 * <p>Используется в административном API при {@code POST /admin/compilations}.</p>
 *
 * <ul>
 *   <li>{@code events} — список идентификаторов событий для включения в подборку (может быть пустым);</li>
 *   <li>{@code pinned} — признак закреплённой подборки на главной странице (по умолчанию {@code false});</li>
 *   <li>{@code title} — название подборки, обязательное, длиной от 1 до 50 символов.</li>
 * </ul>
 */
@Data
public class NewCompilationDto {

    @Nullable
    private List<Long> events;

    private Boolean pinned = false;

    @NotBlank(message = "Название подборки не может быть пустым")
    @Size(min = 1, max = 50)
    private String title;

}