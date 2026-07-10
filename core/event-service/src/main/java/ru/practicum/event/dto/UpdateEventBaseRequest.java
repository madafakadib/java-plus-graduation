package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.dto.LocationDto;

import java.time.LocalDateTime;

import static ru.practicum.dto.Constants.FORMAT_DATETIME;

/**
 * Базовый DTO для обновления события.
 * <p>Содержит общий набор полей, которые могут изменяться как пользователями, так и администраторами.</p>
 *
 * <ul>
 *   <li>{@code annotation} — краткая аннотация, от 20 до 2000 символов;</li>
 *   <li>{@code category} — новая категория события (идентификатор, положительное число);</li>
 *   <li>{@code description} — полное описание, от 20 до 7000 символов;</li>
 *   <li>{@code eventDate} — новая дата и время проведения события,
 *       должна быть в будущем, формат {@link ru.practicum.dto.Constants#FORMAT_DATETIME};</li>
 *   <li>{@code location} — новые координаты события ({@link ru.practicum.dto.LocationDto});</li>
 *   <li>{@code paid} — признак платного участия;</li>
 *   <li>{@code participantLimit} — лимит участников (0 или положительное число);</li>
 *   <li>{@code requestModeration} — нужно ли подтверждать заявки пользователей;</li>
 *   <li>{@code title} — новый заголовок события, от 3 до 120 символов.</li>
 * </ul>
 *
 * <p>Используется как родитель для:
 * {@link UpdateEventUserRequest} и {@link UpdateEventAdminRequest}.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventBaseRequest {
    @Size(min = 20, max = 2000, message = "Аннотация должна быть от 20 до 2000 символов")
    private String annotation;

    @Positive(message = "Категория должна быть положительным числом")
    private Long category;

    @Size(min = 20, max = 7000, message = "Описание должно быть от 20 до 7000 символов")
    private String description;

    @DateTimeFormat(pattern = FORMAT_DATETIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FORMAT_DATETIME)
    @Future
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Лимит участников должен быть 0 или положительным числом")
    private Integer participantLimit;

    private Boolean requestModeration;

    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3 до 120 символов")
    private String title;
}
