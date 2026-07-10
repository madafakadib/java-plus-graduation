package ru.practicum.event.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.practicum.event.model.PrivateStateAction;
/**
 * DTO для обновления события пользователем.
 * <p>Расширяет {@link UpdateEventBaseRequest}, добавляя поле действия пользователя над событием.</p>
 *
 * <ul>
 *   <li>{@code stateAction} — действие, которое может выполнить инициатор события
 *       (например, отправка на модерацию или отмена), см. {@link ru.practicum.event.model.PrivateStateAction}.</li>
 * </ul>
 *
 * <p>Используется в приватном API при {@code PATCH /users/{userId}/events/{eventId}}.</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest extends UpdateEventBaseRequest {

    private PrivateStateAction stateAction;
}
