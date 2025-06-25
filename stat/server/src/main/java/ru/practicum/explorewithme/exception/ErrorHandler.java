package ru.practicum.explorewithme.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage invalidDateExceptionHandler(InvalidDateException e) {
        return ErrorMessage.builder()
                .error("Invalid date")
                .message("Dates in query are invalid")
                .description(String.format("In your query, the beginning of %s is later than the end of %s, " +
                        "which is impossible", e.getStart().toString(), e.getEnd().toString()))
                .build();
    }
}
