package ru.practicum.explorewithme.exception;

import org.jooq.exception.DataException;

public class IllegalRequestStatusException extends DataException {
    public IllegalRequestStatusException(String message) {
        super(message);
    }
}
