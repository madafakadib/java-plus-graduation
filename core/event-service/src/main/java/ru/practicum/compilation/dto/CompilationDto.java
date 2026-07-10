package ru.practicum.compilation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.practicum.dto.EventShortDto;

import java.util.List;
/**
 * DTO подборки событий.
 * <p>Используется в публичном и административном API.</p>
 *
 * <ul>
 *   <li>{@code id} — идентификатор подборки (только для чтения);</li>
 *   <li>{@code events} — список кратких DTO событий, входящих в подборку
 *       ({@link ru.practicum.dto.EventShortDto});</li>
 *   <li>{@code pinned} — признак закреплённой подборки на главной странице
 *       (по умолчанию {@code false});</li>
 *   <li>{@code title} — название подборки, обязательное, не пустое.</li>
 * </ul>
 */
@Data
public class CompilationDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private List<EventShortDto> events;

    private Boolean pinned = false;

    @NotBlank(message = "Название подборки не может быть пустым")
    private String title;

}