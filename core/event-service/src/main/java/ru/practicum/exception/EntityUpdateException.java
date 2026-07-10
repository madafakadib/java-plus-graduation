package ru.practicum.exception;
/**
 * Исключение, сигнализирующее об ошибке при обновлении сущности.
 * <p>Выбрасывается, когда операция обновления невозможна
 * из-за нарушения бизнес-правил или состояния объекта.</p>
 *
 * <p>Сообщение формируется как {@code <имя_сущности> + message}, где
 * {@code entityClass} — класс сущности.</p>
 *
 * <p>Примеры использования:</p>
 * <ul>
 *   <li>попытка обновить событие в недопустимом статусе;</li>
 *   <li>изменение категории, которая не существует;</li>
 *   <li>редактирование данных, которые нарушают ограничения.</li>
 * </ul>
 */
public class EntityUpdateException extends RuntimeException {
    public EntityUpdateException(Class<?> entityClass, String message) {
        super(entityClass.getSimpleName() + message);
    }
}