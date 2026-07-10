package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * DTO для создания нового комментария.
 * <p>Используется в приватном API при добавлении комментария к событию.</p>
 *
 * <ul>
 *   <li>{@code text} — обязательный текст комментария,
 *       длиной от 1 до 2000 символов.</li>
 * </ul>
 */
@Getter
public class InputCommentDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 1, max = 2000)
    private String text;
}