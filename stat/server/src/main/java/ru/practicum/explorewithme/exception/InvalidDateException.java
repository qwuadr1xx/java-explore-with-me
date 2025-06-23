package ru.practicum.explorewithme.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InvalidDateException extends RuntimeException {
    private LocalDateTime start;
    private LocalDateTime end;
}
