package ru.practicum.explorewithme.exception;

import jakarta.validation.ValidationException;

public class InvalidStateException extends ValidationException {
    public InvalidStateException(String message) {
        super(message);
    }
}
