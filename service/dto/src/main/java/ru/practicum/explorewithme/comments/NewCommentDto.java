package ru.practicum.explorewithme.comments;

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
public class NewCommentDto {
    @NotBlank(message = "Field: content. Error: must not be blank or null. Value: '' or null")
    @Size(min = 3, max = 2000)
    private String content;
}
