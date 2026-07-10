package ru.practicum.exception;
/**
 * Исключение, сигнализирующее о превышении лимита участников события.
 * <p>Выбрасывается при попытке подтвердить или добавить новую заявку,
 * если установленный {@code participantLimit} уже достигнут.</p>
 *
 * <p>Примеры использования:</p>
 * <ul>
 *   <li>попытка подать заявку на участие в событии с заполненным лимитом;</li>
 *   <li>подтверждение новой заявки модератором после достижения лимита;</li>
 *   <li>массовая операция, которая превысила бы количество доступных мест.</li>
 * </ul>
 */
public class ParticipantLimitException extends RuntimeException {
    public ParticipantLimitException(String message) {
        super(message);
    }
}