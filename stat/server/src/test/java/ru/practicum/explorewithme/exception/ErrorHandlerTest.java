package ru.practicum.explorewithme.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void invalidDateExceptionHandler_shouldReturnCorrectError() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 24, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 6, 23, 10, 0);

        InvalidDateException ex = new InvalidDateException(start, end);

        ErrorMessage message = errorHandler.invalidDateExceptionHandler(ex);

        assertThat(message).isNotNull();
        assertThat(message.getError()).isEqualTo("Invalid date");
        assertThat(message.getMessage()).isEqualTo("Dates in query are invalid");
        assertThat(message.getDescription()).contains("beginning of 2025-06-24T10:00 is later than the end of 2025-06-23T10:00");
    }
}
