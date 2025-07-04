package ru.practicum.explorewithme.exception;

import jakarta.validation.ValidationException;
import org.jooq.exception.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(ValidationException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status("BAD_REQUEST")
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Requested resource was not found.")
                .status("NOT_FOUND")
                .build();
    }

    @ExceptionHandler(IllegalDateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIllegalDateException(IllegalDateException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrectly DateTime in request.")
                .status("CONFLICT")
                .build();
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerDataAccessException(DataAccessException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Conflict with database")
                .status("CONFLICT")
                .build();
    }
}
