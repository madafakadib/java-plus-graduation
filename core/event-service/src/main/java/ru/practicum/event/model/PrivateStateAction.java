package ru.practicum.event.model;
/**
 * Действия инициатора события (пользователя) над своим событием.
 * <p>Используется в {@link ru.practicum.event.dto.UpdateEventUserRequest}.</p>
 *
 * <ul>
 *   <li>{@link #SEND_TO_REVIEW} — отправить событие на модерацию;</li>
 *   <li>{@link #CANCEL_REVIEW} — отменить поданную на модерацию заявку.</li>
 * </ul>
 */
public enum PrivateStateAction {
    SEND_TO_REVIEW,
    CANCEL_REVIEW
}
