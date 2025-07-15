package ru.practicum.explorewithme.categories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class NewCategoryDto {
    @NotBlank(message = "Field: name. Error: must not be blank or null. Value: '' or null")
    @Size(min = 1, max = 50)
    private String name;
}
