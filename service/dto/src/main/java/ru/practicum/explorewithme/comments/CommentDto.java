package ru.practicum.explorewithme.comments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.comments.util.CommentStatus;
import ru.practicum.explorewithme.users.UserShortDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CommentDto {
    private Long id;

    private String content;

    private Long eventId;

    private UserShortDto author;

    private CommentStatus status;

    private LocalDateTime createdOn;
}
