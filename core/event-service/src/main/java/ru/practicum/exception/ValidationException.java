package ru.practicum.exception;
/**
 * Исключение, сигнализирующее о нарушении правил валидации на уровне бизнес-логики.
 * <p>Отличается от {@link jakarta.validation.ConstraintViolationException} тем,
 * что выбрасывается вручную из сервисного слоя при проверках,
 * которые невозможно выразить аннотациями Bean Validation.</p>
 *
 * <p>Сообщение формируется как {@code <имя_сущности> + message}, где
 * {@code entityClass} — класс сущности, валидация которой провалилась.</p>
 *
 * <p>Примеры использования:</p>
 * <ul>
 *   <li>попытка создать событие, начинающееся менее чем через 2 часа;</li>
 *   <li>инициатор пытается комментировать собственное событие;</li>
 *   <li>заявка на участие в неопубликованном событии.</li>
 * </ul>
 */
public class ValidationException extends RuntimeException {

    public ValidationException(Class<?> entityClass, String message) {
        super(entityClass.getSimpleName() + message);
    }
}