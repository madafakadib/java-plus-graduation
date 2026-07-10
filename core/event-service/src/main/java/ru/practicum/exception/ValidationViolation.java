package ru.practicum.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
/**
 * DTO, описывающее отдельное нарушение валидации.
 * <p>Используется в {@link ValidationErrorResponse} для передачи клиенту
 * информации о том, какое поле не прошло проверку и по какой причине.</p>
 *
 * <p>Примеры:</p>
 * <ul>
 *   <li>{@code fieldName = "email", message = "Некорректный формат"};</li>
 *   <li>{@code fieldName = "title", message = "Размер должен быть от 3 до 120 символов"}.</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public class ValidationViolation {

    private final String fieldName;
    private final String message;

}