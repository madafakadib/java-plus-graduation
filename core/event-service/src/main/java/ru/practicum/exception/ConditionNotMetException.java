package ru.practicum.exception;
/**
 * Исключение, сигнализирующее о нарушении бизнес-условий.
 * <p>Выбрасывается, когда выполняемая операция невозможна из-за
 * несоответствия состоянию сущности или правилам доменной логики.</p>
 *
 * <p>Примеры использования:</p>
 * <ul>
 *   <li>попытка опубликовать событие, которое не находится в статусе {@code PENDING};</li>
 *   <li>изменение даты события ближе, чем за час до публикации;</li>
 *   <li>отклонение уже опубликованного события.</li>
 * </ul>
 */
public class ConditionNotMetException extends RuntimeException {

    public ConditionNotMetException(String message) {
        super(message);
    }

}