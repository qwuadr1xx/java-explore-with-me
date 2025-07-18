package ru.practicum.explorewithme.internal.comments.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.comments.CommentDto;
import ru.practicum.explorewithme.comments.NewCommentDto;
import ru.practicum.explorewithme.comments.util.CommentStatus;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.utils.RecordToCommentMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static ru.practicum.explorewithme.jooq.tables.Comments.COMMENTS;
import static ru.practicum.explorewithme.jooq.tables.Users.USERS;

@Repository
@RequiredArgsConstructor
public class PrivateCommentsRepositoryImpl implements CommentsRepository {
    private final DSLContext dsl;
    private static final Set<Field<?>> COMMENT_FIELDS = Set.of(
            COMMENTS.ID,
            COMMENTS.CONTENT,
            COMMENTS.CREATED,
            COMMENTS.EVENT_ID,
            COMMENTS.STATUS,
            USERS.ID,
            USERS.NAME
    );

    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        checkUser(userId);
        checkEvent(eventId);

        Long comId = dsl.insertInto(COMMENTS)
                .set(COMMENTS.CONTENT, newCommentDto.getContent())
                .set(COMMENTS.AUTHOR_ID, userId)
                .set(COMMENTS.EVENT_ID, eventId)
                .set(COMMENTS.STATUS, CommentStatus.PENDING.toString())
                .set(COMMENTS.CREATED, LocalDateTime.now())
                .returning(COMMENTS.ID)
                .fetchOptional()
                .orElseThrow(() -> new DataAccessException("Comment was not added"))
                .getId();

        return dsl.select(COMMENT_FIELDS)
                .from(COMMENTS)
                .join(USERS).on(USERS.ID.eq(COMMENTS.AUTHOR_ID))
                .where(COMMENTS.ID.eq(comId))
                .fetchOptional()
                .orElseThrow(() -> new DataAccessException("Comment was not added"))
                .map(RecordToCommentMapper::map);
    }

    @Override
    public List<CommentDto> getUserCommentsOnEvent(Long userId, Long eventId, CommentStatus status, Integer from, Integer size) {
        checkUser(userId);
        checkEvent(eventId);

        return dsl.select(COMMENT_FIELDS)
                .from(COMMENTS)
                .join(USERS).on(USERS.ID.eq(COMMENTS.AUTHOR_ID))
                .where(COMMENTS.EVENT_ID.eq(eventId))
                .and(COMMENTS.AUTHOR_ID.eq(userId))
                .and(COMMENTS.STATUS.eq(status.toString()))
                .offset(from)
                .limit(size)
                .fetch()
                .map(RecordToCommentMapper::map);
    }

    @Override
    public List<CommentDto> getUserComments(Long userId, CommentStatus status, Integer from, Integer size) {
        checkUser(userId);

        return dsl.select(COMMENT_FIELDS)
                .from(COMMENTS)
                .join(USERS).on(USERS.ID.eq(COMMENTS.AUTHOR_ID))
                .and(COMMENTS.AUTHOR_ID.eq(userId))
                .and(COMMENTS.STATUS.eq(status.toString()))
                .offset(from)
                .limit(size)
                .fetch()
                .map(RecordToCommentMapper::map);
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto) {
        if (dsl.update(COMMENTS)
                .set(COMMENTS.CONTENT, newCommentDto.getContent())
                .where(COMMENTS.ID.eq(commentId))
                .and(COMMENTS.AUTHOR_ID.eq(userId))
                .and(COMMENTS.STATUS.eq(CommentStatus.PENDING.toString()))
                .execute() == 0) {
            throw new DataAccessException("Comment was not updated");
        }

        return dsl.select(COMMENT_FIELDS)
                .from(COMMENTS)
                .join(USERS).on(USERS.ID.eq(COMMENTS.AUTHOR_ID))
                .where(COMMENTS.ID.eq(commentId))
                .fetchOptional()
                .orElseThrow(() -> new DataAccessException("Comment was not updated"))
                .map(RecordToCommentMapper::map);
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        if (dsl.deleteFrom(COMMENTS)
                .where(COMMENTS.ID.eq(commentId))
                .and(COMMENTS.AUTHOR_ID.eq(userId))
                .execute() == 0) {
            throw new NotFoundException("Comment with id " + commentId + " was not found");
        }
    }

    private void checkUser(Long userId) {
        dsl.selectFrom(USERS)
                .where(USERS.ID.eq(userId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("User with id %s does not exist", userId)));
    }

    private void checkEvent(Long eventId) {
        dsl.selectFrom(Events.EVENTS)
                .where(Events.EVENTS.ID.eq(eventId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s does not exist", eventId)));
    }
}
