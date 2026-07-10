package ru.practicum.exception;


import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Глобальный обработчик ошибок уровня веб-слоя.
 * <p>Преобразует исключения в унифицированные HTTP-ответы
 * с корректным статус-кодом и телом {@link ApiError} либо {@link ValidationErrorResponse}.</p>
 *
 * <p>Карта соответствий:</p>
 * <ul>
 *   <li>400 BAD_REQUEST — валидационные ошибки {@link jakarta.validation.ConstraintViolationException},
 *       {@link org.springframework.web.bind.MethodArgumentNotValidException},
 *       {@link org.springframework.web.bind.MissingServletRequestParameterException},
 *       а также доменная {@link ValidationException};</li>
 *   <li>404 NOT_FOUND — отсутствующие сущности ({@link EntityNotFoundException}) и избыточные операции
 *       ({@link OperationUnnecessaryException});</li>
 *   <li>403 FORBIDDEN — бизнес-запрет на обновление ({@link EntityUpdateException});</li>
 *   <li>409 CONFLICT — нарушение условий операции ({@link ConditionNotMetException}),
 *       нарушение целостности БД ({@link org.springframework.dao.DataIntegrityViolationException}),
 *       и конфликтные доменные ситуации (например, {@link NotPublishEventException},
 *       {@link InitiatorRequestException}, {@link ParticipantLimitException},
 *       {@link RepeatUserRequestorException});</li>
 *   <li>500 INTERNAL_SERVER_ERROR — любые необработанные ошибки.</li>
 * </ul>
 *
 * <p>Все случаи логируются через {@code Slf4j} c stack trace.</p>
 */
@Slf4j
@RestControllerAdvice
public class ErrorHandlerControllerAdvice {

    /**
     * 400: Валидация параметров (bean validation на @RequestParam/@PathVariable и т.п.).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse onConstraintValidationException(
            ConstraintViolationException e
    ) {
        final List<ValidationViolation> validationViolations = e.getConstraintViolations().stream()
                .map(
                        violation -> {
                            log.error("ConstraintViolationException: {} : {}", violation.getPropertyPath().toString(), violation.getMessage());
                            return new ValidationViolation(
                                    violation.getPropertyPath().toString(),
                                    violation.getMessage()
                            );
                        }
                )
                .collect(Collectors.toList());

        return new ValidationErrorResponse(validationViolations);
    }
    /**
     * 400: Ошибки биндинга тела запроса и отсутствие обязательных параметров.
     */
    @ExceptionHandler({MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationErrorResponse onMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        final List<ValidationViolation> validationViolations = e.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                            log.error("MethodArgumentNotValidException: {} : {}", error.getField(), error.getDefaultMessage());
                            return new ValidationViolation(error.getField(), error.getDefaultMessage());
                        }
                )
                .collect(Collectors.toList());

        return new ValidationErrorResponse(validationViolations);
    }

    /**
     * 404: Сущность не найдена / операция не требуется.
     */
    @ExceptionHandler({EntityNotFoundException.class, OperationUnnecessaryException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError onEntityNotFoundException(final EntityNotFoundException e) {
        log.error("EntityNotFoundException - 404: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError("NOT_FOUND", "entity not found", stackTrace, LocalDateTime.now().toString());
    }
    /**
     * 403: Обновление запрещено бизнес-правилами.
     */
    @ExceptionHandler({EntityUpdateException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError onEntityUpdateException(final EntityUpdateException e) {
        log.error("EntityUpdateException - 409: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError("FORBIDDEN", "entity update forbidden", stackTrace, LocalDateTime.now().toString());
    }
    /**
     * 409: Условия операции не соблюдены (конкуренция состояний и т.п.).
     */
    @ExceptionHandler({ConditionNotMetException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError onConditionNotMetException(final ConditionNotMetException e) {
        log.error("ConditionNotMetException - 409: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError(
                "CONFLICT",
                "For the requested operation the conditions are not met.",
                stackTrace,
                LocalDateTime.now().toString()
        );
    }
    /**
     * 409: Нарушение ограничений целостности БД (уникальные ключи, FK и др.).
     */
    @ExceptionHandler({DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError onDataIntegrityViolationException(final DataIntegrityViolationException e) {
        log.error("DataIntegrityViolationException - 409: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError("CONFLICT", "Integrity constraint has been violated", stackTrace, LocalDateTime.now().toString());
    }
    /**
     * 500: Любые неперехваченные исключения.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleAnyException(final Throwable e) {
        log.error("Error:500; {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();

        return new ApiError("INTERNAL_SERVER_ERROR", "internal server error", stackTrace, LocalDateTime.now().toString());
    }
    /**
     * 400: Доменные валидационные ошибки.
     */
    @ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError validationException(final ValidationException e) {
        log.error("ValidationException - 400: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError("BAD_REQUEST", "validation exception", stackTrace, LocalDateTime.now().toString());
    }
    /**
     * 409: Конфликтные доменные состояния (неопубликованное событие, лимиты и пр.).
     */
    @ExceptionHandler({NotPublishEventException.class,
            InitiatorRequestException.class,
            ParticipantLimitException.class,
            RepeatUserRequestorException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError onNotPublishEventException(final RuntimeException e) {
        log.error("409: {}", e.getMessage(), e);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        return new ApiError("CONFLICT", "event is not published", stackTrace, LocalDateTime.now().toString());
    }
    /**
     * Унифицированное тело ошибки, возвращаемое клиенту.
     *
     * @param status    символьный статус (например, {@code "CONFLICT"})
     * @param reason    краткое человекочитаемое описание причины
     * @param message   подробности/stack trace (для дебага; потенциально стоит скрывать в prod)
     * @param timestamp момент формирования ответа
     */
    public record ApiError(String status, String reason, String message, String timestamp) {
    }
}