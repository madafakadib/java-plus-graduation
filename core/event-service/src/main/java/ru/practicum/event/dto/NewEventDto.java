package ru.practicum.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.dto.LocationDto;
/**
 * DTO для создания нового события.
 * <p>Используется в приватном API при {@code POST /users/{userId}/events}.</p>
 *
 * <ul>
 *   <li>{@code annotation} — краткая аннотация события, обязательная, от 20 до 2000 символов;</li>
 *   <li>{@code category} — идентификатор категории, обязательный;</li>
 *   <li>{@code description} — полное описание события, обязательное, от 20 до 7000 символов;</li>
 *   <li>{@code eventDate} — дата и время проведения события (формат {@link ru.practicum.dto.Constants#FORMAT_DATETIME});</li>
 *   <li>{@code location} — географическая локация события ({@link ru.practicum.dto.LocationDto});</li>
 *   <li>{@code paid} — признак платного участия;</li>
 *   <li>{@code participantLimit} — лимит участников (ноль = без ограничений);</li>
 *   <li>{@code requestModeration} — нужно ли подтверждать заявки пользователей;</li>
 *   <li>{@code title} — заголовок события, обязательный, от 3 до 120 символов.</li>
 * </ul>
 */
@Data
@AllArgsConstructor
public class NewEventDto {
    @NotBlank(message = "Аннотация не может быть пустой")
    @Size(min = 20, max = 2000)
    private String annotation;
    private Long category;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 20, max = 7000)
    private String description;
    private String eventDate;
    private LocationDto location;
    private Boolean paid;

    @PositiveOrZero
    private Long participantLimit;
    private Boolean requestModeration;

    @NotBlank(message = "Заголовок не может быть пустым")
    @Size(min = 3, max = 120)
    private String title;
}