package ru.practicum.explorewithme.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorMessage {
    private String error;

    private String message;

    private String description;
}
