package ru.practicum.category.exception;
/**
 * Исключение выбрасывается, если категория не найдена.
 * <p>Используется в {@code CategoryService} и контроллерах при обращении
 * к несуществующему идентификатору категории.</p>
 */
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
