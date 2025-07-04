package ru.practicum.explorewithme.complitations;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class NewCompilationDto {
    private List<Long> events;

    private Boolean pinned;

    @NotNull(message = "Field: title. Error: must not be blank. Value: null")
    @Size(min = 1, max = 50)
    private String title;
}
