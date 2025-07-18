package ru.practicum.explorewithme.comments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.users.UserShortDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CommentDtoShort {
    private Long id;

    private String content;

    private UserShortDto author;

    private LocalDateTime createdOn;
}
