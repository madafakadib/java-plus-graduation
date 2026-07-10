package ru.practicum.request.exception;

public class NotPublishEventException extends RuntimeException {
    public NotPublishEventException(String message) {
        super(message);
    }
}