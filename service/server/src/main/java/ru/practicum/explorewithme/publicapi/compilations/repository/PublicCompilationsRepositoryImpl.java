package ru.practicum.explorewithme.publicapi.compilations.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.comments.CommentDtoShort;
import ru.practicum.explorewithme.comments.util.CommentStatus;
import ru.practicum.explorewithme.events.EventShortDto;
import ru.practicum.explorewithme.jooq.Tables;
import ru.practicum.explorewithme.compilations.CompilationDto;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.jooq.tables.Compilations;
import ru.practicum.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.utils.RecordToShortCommentMapper;
import ru.practicum.explorewithme.utils.RecordToShortEventMapper;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.*;
import static ru.practicum.explorewithme.jooq.Tables.COMMENTS;
import static ru.practicum.explorewithme.jooq.tables.Categories.CATEGORIES;
import static ru.practicum.explorewithme.jooq.tables.CompilationEvents.COMPILATION_EVENTS;
import static ru.practicum.explorewithme.jooq.tables.Compilations.COMPILATIONS;
import static ru.practicum.explorewithme.jooq.tables.Events.EVENTS;
import static ru.practicum.explorewithme.jooq.tables.Users.USERS;

@Repository
@RequiredArgsConstructor
public class PublicCompilationsRepositoryImpl implements CompilationsRepository {
    private final DSLContext dsl;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        var multisetField = multiset(
                select(
                        EVENTS.ID,
                        EVENTS.TITLE,
                        EVENTS.ANNOTATION,
                        EVENTS.EVENT_DATE,
                        EVENTS.PAID,
                        EVENTS.PARTICIPANT_LIMIT,
                        EVENTS.CONFIRMED_REQUESTS,
                        EVENTS.VIEWS,
                        CATEGORIES.ID,
                        CATEGORIES.NAME,
                        USERS.ID,
                        USERS.NAME,
                        buildCommentsMultisetField()
                )
                        .from(COMPILATION_EVENTS)
                        .join(EVENTS)
                        .on(EVENTS.ID.eq(COMPILATION_EVENTS.EVENT_ID))
                        .join(CATEGORIES)
                        .on(CATEGORIES.ID.eq(EVENTS.CATEGORY_ID))
                        .join(USERS)
                        .on(USERS.ID.eq(EVENTS.INITIATOR_ID))
                        .where(COMPILATION_EVENTS.COMPILATION_ID.eq(COMPILATIONS.ID))).as("events");
        return dsl.select(
                        COMPILATIONS.ID,
                        COMPILATIONS.PINNED,
                        COMPILATIONS.TITLE,
                        multisetField
                )
                .from(COMPILATIONS)
                .where(Optional.ofNullable(pinned)
                        .map(it -> COMPILATIONS.PINNED.eq(pinned))
                        .orElse(trueCondition())
                )
                .offset(from)
                .limit(size)
                .fetch(it -> CompilationDto.builder()
                        .id(it.get(Tables.COMPILATIONS.ID))
                        .title(it.get(Tables.COMPILATIONS.TITLE))
                        .pinned(it.get(Tables.COMPILATIONS.PINNED))
                        .events(it.get(multisetField).map(event -> {
                            EventShortDto eventShort = RecordToShortEventMapper.map(event);
                            eventShort.setComments(event.get(buildCommentsMultisetField()));
                            return eventShort;
                        }))
                        .build());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        var multisetField = multiset(
                select(
                        EVENTS.ID,
                        EVENTS.TITLE,
                        EVENTS.ANNOTATION,
                        EVENTS.EVENT_DATE,
                        EVENTS.PAID,
                        EVENTS.PARTICIPANT_LIMIT,
                        EVENTS.CONFIRMED_REQUESTS,
                        EVENTS.VIEWS,
                        CATEGORIES.ID,
                        CATEGORIES.NAME,
                        USERS.ID,
                        USERS.NAME,
                        buildCommentsMultisetField()
                ).from(COMPILATION_EVENTS)
                        .join(EVENTS)
                        .on(EVENTS.ID.eq(COMPILATION_EVENTS.EVENT_ID))
                        .join(CATEGORIES)
                        .on(CATEGORIES.ID.eq(EVENTS.CATEGORY_ID))
                        .join(USERS)
                        .on(USERS.ID.eq(EVENTS.INITIATOR_ID))
                        .where(COMPILATION_EVENTS.COMPILATION_ID.eq(COMPILATIONS.ID))
        ).as("events");

        return dsl.select(
                        COMPILATIONS.ID,
                        COMPILATIONS.PINNED,
                        COMPILATIONS.TITLE,
                        multisetField
                )
                .from(COMPILATIONS)
                .where(Compilations.COMPILATIONS.ID.eq(compId))
                .fetchOptional(it -> CompilationDto.builder()
                        .id(it.get(Tables.COMPILATIONS.ID))
                        .title(it.get(Tables.COMPILATIONS.TITLE))
                        .pinned(it.get(Tables.COMPILATIONS.PINNED))
                        .events(it.get(multisetField).map(event -> {
                            EventShortDto eventShort = RecordToShortEventMapper.map(event);
                            eventShort.setComments(event.get(buildCommentsMultisetField()));
                            return eventShort;
                        }))
                        .build())
                .orElseThrow(() -> new NotFoundException(String.format("Compilation with id %s does not exist.",
                        compId)));
    }

    private Field<List<CommentDtoShort>> buildCommentsMultisetField() {
        return multiset(
                select(
                        COMMENTS.ID,
                        COMMENTS.CONTENT,
                        COMMENTS.CREATED,
                        Tables.USERS.ID,
                        Tables.USERS.NAME
                )
                        .from(COMMENTS)
                        .join(Tables.USERS).on(Tables.USERS.ID.eq(COMMENTS.AUTHOR_ID))
                        .where(COMMENTS.EVENT_ID.eq(Events.EVENTS.ID))
                        .and(COMMENTS.STATUS.eq(CommentStatus.APPROVED.toString()))
        ).convertFrom(r -> r.map(RecordToShortCommentMapper::map))
                .as("comments");
    }
}
