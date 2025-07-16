package ru.practicum.explorewithme.events;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Location {
    @NotNull(message = "Field: lat. Error: must not be blank. Value: ''")
    private Double lat;

    @NotNull(message = "Field: lon. Error: must not be blank. Value: ''")
    private Double lon;
}
