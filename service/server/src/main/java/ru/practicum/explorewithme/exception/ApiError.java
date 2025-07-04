package ru.practicum.explorewithme.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class ApiError {
    private String message;

    private String reason;

    private String status;

    private LocalDateTime timestamp = LocalDateTime.now();
}
