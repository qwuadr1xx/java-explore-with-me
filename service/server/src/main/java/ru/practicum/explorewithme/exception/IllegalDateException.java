package ru.practicum.explorewithme.exception;

import org.jooq.exception.DataAccessException;

public class IllegalDateException extends DataAccessException {
    public IllegalDateException(String message) {
        super(message);
    }
}
