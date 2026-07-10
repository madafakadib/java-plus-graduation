package ru.practicum.event.model;
/**
 * Действия администратора над событием.
 * <p>Используется в {@link ru.practicum.event.dto.UpdateEventAdminRequest} для изменения состояния события.</p>
 *
 * <ul>
 *   <li>{@link #PUBLISH_EVENT} — опубликовать событие;</li>
 *   <li>{@link #REJECT_EVENT} — отклонить событие.</li>
 * </ul>
 */
public enum AdminStateAction {

    PUBLISH_EVENT,
    REJECT_EVENT

}
