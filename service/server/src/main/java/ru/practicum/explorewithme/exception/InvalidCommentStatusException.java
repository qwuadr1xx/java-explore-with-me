package ru.practicum.explorewithme.exception;

import org.jooq.exception.DataAccessException;

public class InvalidCommentStatusException extends DataAccessException {
    public InvalidCommentStatusException(String message) {
        super(message);
    }
}
