package ru.practicum.exception;
/**
 * Исключение, сигнализирующее о попытке операции над неопубликованным событием.
 * <p>Выбрасывается, когда пользователь или сервис обращается к событию,
 * которое ещё не прошло модерацию и не имеет статуса {@code PUBLISHED}.</p>
 *
 * <p>Примеры использования:</p>
 * <ul>
 *   <li>попытка оставить комментарий к неопубликованному событию;</li>
 *   <li>добавление лайка или запроса на участие в событии, которое не опубликовано;</li>
 *   <li>поиск события по id, когда оно находится в статусе {@code PENDING} или {@code CANCELED}.</li>
 * </ul>
 */
public class NotPublishEventException extends RuntimeException {
    public NotPublishEventException(String message) {
        super(message);
    }
}