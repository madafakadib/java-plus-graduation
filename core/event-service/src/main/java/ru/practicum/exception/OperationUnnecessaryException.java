package ru.practicum.exception;
/**
 * Исключение, сигнализирующее о ненужности выполняемой операции.
 * <p>Выбрасывается, когда действие над сущностью не имеет смысла
 * или дублирует уже достигнутое состояние.</p>
 *
 * <p>Примеры использования:</p>
 * <ul>
 *   <li>попытка повторно отменить уже отменённое событие;</li>
 *   <li>попытка опубликовать событие, которое уже опубликовано;</li>
 *   <li>удаление объекта, который и так отсутствует в системе.</li>
 * </ul>
 */
public class OperationUnnecessaryException extends RuntimeException {
    public OperationUnnecessaryException(String message) {
        super(message);
    }
}
