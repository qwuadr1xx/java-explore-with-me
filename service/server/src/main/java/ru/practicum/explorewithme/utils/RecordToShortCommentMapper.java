package ru.practicum.explorewithme.utils;

import org.jooq.Record;
import ru.practicum.explorewithme.comments.CommentDtoShort;
import ru.practicum.explorewithme.users.UserShortDto;

import static ru.practicum.explorewithme.jooq.tables.Comments.COMMENTS;
import static ru.practicum.explorewithme.jooq.tables.Users.USERS;

public class RecordToShortCommentMapper {
    public static CommentDtoShort map(Record record) {
        return CommentDtoShort.builder()
                .id(record.get(COMMENTS.ID))
                .content(record.get(COMMENTS.CONTENT))
                .createdOn(record.get(COMMENTS.CREATED))
                .author(UserShortDto.builder()
                        .id(record.get(USERS.ID))
                        .name(record.get(USERS.NAME))
                        .build())
                .build();
    }
}
