package ru.practicum.explorewithme.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.jooq.exception.DataAccessException;
import org.postgresql.util.PSQLException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({ConstraintViolationException.class, ValidationException.class, BadRequestException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(RuntimeException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Requested resource was not found.")
                .status("NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(IllegalDateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalDateException(IllegalDateException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrectly DateTime in request.")
                .status("BAD_REQUEST")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(IllegalRequestStatusException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIllegalRequestStatusException(IllegalRequestStatusException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Illegal request status.")
                .status("CONFLICT")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(IllegalEventStatusException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIllegalEventStatusException(IllegalEventStatusException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Illegal event status.")
                .status("CONFLICT")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIllegalArgumentException(IllegalArgumentException e) {
        e.printStackTrace();
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Illegal argument.")
                .status("CONFLICT")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(InvalidStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleInvalidStateException(InvalidStateException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Invalid state.")
                .status("CONFLICT")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(PSQLException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerPSQLException(PSQLException e) {
        return switch (e.getSQLState()) {
            case "23505" -> ApiError.builder()
                    .message(e.getLocalizedMessage())
                    .reason("Not unique value")
                    .status("CONFLICT")
                    .timestamp(LocalDateTime.now())
                    .build();
            case "23503" -> ApiError.builder()
                    .message(e.getLocalizedMessage())
                    .reason("Foreign key constraint violation")
                    .status("CONFLICT")
                    .timestamp(LocalDateTime.now())
                    .build();
            default -> ApiError.builder()
                    .message(e.getLocalizedMessage())
                    .reason("Unexpected database error.")
                    .status("CONFLICT")
                    .timestamp(LocalDateTime.now())
                    .build();
        };
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Incorrectly made request.")
                .status("CONFLICT")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handlerDataAccessException(DataAccessException e) {
        return ApiError.builder()
                .message(e.getMessage())
                .reason("Conflict with database")
                .status("CONFLICT")
                .timestamp(LocalDateTime.now())
                .build();
    }
}
