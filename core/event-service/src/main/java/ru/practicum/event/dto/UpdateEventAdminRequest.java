package ru.practicum.event.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.practicum.event.model.AdminStateAction;
/**
 * DTO для обновления события администратором.
 * <p>Расширяет {@link UpdateEventBaseRequest}, добавляя административное действие над событием.</p>
 *
 * <ul>
 *   <li>{@code stateAction} — действие администратора над событием
 *       (например, публикация или отклонение), см. {@link ru.practicum.event.model.AdminStateAction}.</li>
 * </ul>
 *
 * <p>Используется в админском API при {@code PATCH /admin/events/{eventId}}.</p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest extends UpdateEventBaseRequest {

    private AdminStateAction stateAction;
}
