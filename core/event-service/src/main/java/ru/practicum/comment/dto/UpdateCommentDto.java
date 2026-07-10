package ru.practicum.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
/**
 * DTO для обновления комментария.
 * <p>Используется в приватном и административном API при редактировании текста комментария.</p>
 *
 * <ul>
 *   <li>{@code text} — новый текст комментария,
 *       обязательный, длиной от 1 до 2000 символов.</li>
 * </ul>
 */
@Data
public class UpdateCommentDto {
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 1, max = 2000)
    private String text;
}
