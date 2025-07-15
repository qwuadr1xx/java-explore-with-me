package ru.practicum.explorewithme.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class NewEventDto {
    @NotBlank(message = "Field: annotation. Error: must not be blank or null. Value: '' or null")
    @Size(min = 20, max = 2000)
    private String annotation;

    @NotNull(message = "Field: category. Error: must not be blank. Value: null")
    private Long category;

    @NotBlank(message = "Field: description. Error: must not be blank or null. Value: '' or null")
    @Size(min = 20, max = 7000)
    private String description;

    @NotNull(message = "Field: eventDate. Error: must not be blank or null. Value: '' or null")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Field: location. Error: must not be blank. Value: null")
    private Location location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    @NotBlank(message = "Field: title. Error: must not be blank or null. Value: '' or null")
    @Size(min = 3, max = 120)
    private String title;
}
