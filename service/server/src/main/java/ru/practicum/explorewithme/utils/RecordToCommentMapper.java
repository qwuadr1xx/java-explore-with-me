package ru.practicum.explorewithme.utils;

import org.jooq.Record;
import ru.practicum.explorewithme.comments.CommentDto;
import ru.practicum.explorewithme.comments.util.CommentStatus;
import ru.practicum.explorewithme.users.UserShortDto;

import static ru.practicum.explorewithme.jooq.tables.Comments.COMMENTS;
import static ru.practicum.explorewithme.jooq.tables.Users.USERS;

public class RecordToCommentMapper {
    public static CommentDto map(Record record) {
        return CommentDto.builder()
                .id(record.get(COMMENTS.ID))
                .content(record.get(COMMENTS.CONTENT))
                .eventId(record.get(COMMENTS.EVENT_ID))
                .createdOn(record.get(COMMENTS.CREATED))
                .status(CommentStatus.valueOf(record.get(COMMENTS.STATUS)))
                .author(UserShortDto.builder()
                        .id(record.get(USERS.ID))
                        .name(record.get(USERS.NAME))
                        .build())
                .build();
    }
}
