package ru.practicum.exception;
/**
 * Исключение, сигнализирующее об отсутствии сущности в хранилище.
 * <p>Выбрасывается при обращении к объекту, которого нет в БД или внешнем сервисе.</p>
 *
 * <p>Сообщение формируется как {@code <имя_сущности> + message}, где
 * {@code entityClass} — класс сущности.</p>
 *
 * <p>Примеры использования:</p>
 * <ul>
 *   <li>поиск события по id, которого нет в БД;</li>
 *   <li>попытка получить категорию или пользователя из внешнего сервиса;</li>
 *   <li>поиск комментария, который был удалён.</li>
 * </ul>
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Class<?> entityClass, String message) {
        super(entityClass.getSimpleName() + message);
    }
}