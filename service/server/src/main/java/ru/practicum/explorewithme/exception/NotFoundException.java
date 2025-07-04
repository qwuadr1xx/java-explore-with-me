package ru.practicum.explorewithme.exception;

import org.jooq.exception.DataAccessException;

public class NotFoundException extends DataAccessException {
    public NotFoundException(String message) {
        super(message);
    }
}
