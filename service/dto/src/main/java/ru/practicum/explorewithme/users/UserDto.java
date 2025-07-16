package ru.practicum.explorewithme.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDto {
    private Long id;

    @NotNull(message = "Field: name. Error: must not be blank. Value: null")
    @Size(min = 2, max = 250)
    private String name;

    @NotNull(message = "Field: email. Error: must not be blank. Value: null")
    @Size(min = 6, max = 254)
    @Email
    private String email;
}
