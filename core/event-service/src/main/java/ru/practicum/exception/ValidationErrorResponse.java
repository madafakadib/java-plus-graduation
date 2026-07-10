package ru.practicum.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
/**
 * DTO-ответ для валидационных ошибок.
 * <p>Используется {@link ru.practicum.exception.ErrorHandlerControllerAdvice}
 * для формирования тела ответа при возникновении ошибок валидации
 * (например, {@link jakarta.validation.ConstraintViolationException}
 * или {@link org.springframework.web.bind.MethodArgumentNotValidException}).</p>
 *
 * <p>Содержит список нарушений {@link ValidationViolation},
 * каждое из которых описывает проблемное поле и сообщение об ошибке.</p>
 */
@Getter
@RequiredArgsConstructor
public class ValidationErrorResponse {

    private final List<ValidationViolation> validationViolations;

}
