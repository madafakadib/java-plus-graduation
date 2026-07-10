package ru.practicum.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.dto.UserShortDto;

import java.time.LocalDateTime;
/**
 * DTO комментария к событию.
 * <p>Используется в публичном, приватном и админском API.</p>
 *
 * <ul>
 *   <li>{@code id} — идентификатор комментария (только для чтения);</li>
 *   <li>{@code eventId} — идентификатор события, к которому относится комментарий;</li>
 *   <li>{@code author} — краткая информация об авторе ({@link ru.practicum.dto.UserShortDto});</li>
 *   <li>{@code text} — текст комментария;</li>
 *   <li>{@code created} — дата и время создания в формате {@code yyyy-MM-dd HH:mm:ss}.</li>
 * </ul>
 */
@Getter
@Setter
public class CommentDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private Long eventId;
    private UserShortDto author;
    private String text;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
}