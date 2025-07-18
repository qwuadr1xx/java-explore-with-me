package ru.practicum.explorewithme.admin.events.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.comments.CommentDto;
import ru.practicum.explorewithme.comments.CommentDtoShort;
import ru.practicum.explorewithme.comments.util.CommentStatus;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.jooq.tables.Categories;
import ru.practicum.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.jooq.tables.Locations;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.events.utils.StateAction;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.jooq.tables.Users;
import ru.practicum.explorewithme.utils.RecordToCommentMapper;
import ru.practicum.explorewithme.utils.RecordToEventMapper;
import ru.practicum.explorewithme.utils.RecordToShortCommentMapper;

import java.time.LocalDateTime;
import java.util.*;

import static org.jooq.impl.DSL.*;
import static ru.practicum.explorewithme.jooq.Tables.COMMENTS;
import static ru.practicum.explorewithme.jooq.Tables.USERS;

@Repository
@RequiredArgsConstructor
public class EventsRepositoryImpl implements EventsRepository {
    private final DSLContext dsl;
    private static final Set<Field<?>> SELECT_FIELDS = Set.of(
            Events.EVENTS.ID,
            Events.EVENTS.ANNOTATION,
            Categories.CATEGORIES.ID,
            Categories.CATEGORIES.NAME,
            Events.EVENTS.CONFIRMED_REQUESTS,
            Events.EVENTS.CREATED_ON,
            Events.EVENTS.DESCRIPTION,
            Events.EVENTS.EVENT_DATE,
            Users.USERS.ID,
            Users.USERS.NAME,
            Locations.LOCATIONS.LAT,
            Locations.LOCATIONS.LON,
            Events.EVENTS.PAID,
            Events.EVENTS.PARTICIPANT_LIMIT,
            Events.EVENTS.PUBLISHED_ON,
            Events.EVENTS.REQUEST_MODERATION,
            Events.EVENTS.STATE,
            Events.EVENTS.TITLE,
            Events.EVENTS.VIEWS
    );

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        Field<List<CommentDtoShort>> comments = buildCommentsMultisetField();
        List<SelectFieldOrAsterisk> fields = new ArrayList<>(SELECT_FIELDS);
        fields.add(comments);

        var query = dsl.select(fields)
                .from(Events.EVENTS)
                .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                .join(Locations.LOCATIONS).on(Locations.LOCATIONS.ID.eq(Events.EVENTS.LOCATION_ID))
                .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                .where(DSL.trueCondition());

        if (users != null && !users.isEmpty()) {
            query = query.and(Events.EVENTS.INITIATOR_ID.in(users));
        }

        if (states != null && !states.isEmpty()) {
            query = query.and(Events.EVENTS.STATE.in(states));
        }

        if (categories != null && !categories.isEmpty()) {
            query = query.and(Events.EVENTS.CATEGORY_ID.in(categories));
        }

        if (rangeStart != null) {
            query = query.and(Events.EVENTS.EVENT_DATE.greaterOrEqual(rangeStart));
        }

        if (rangeEnd != null) {
            query = query.and(Events.EVENTS.EVENT_DATE.lessOrEqual(rangeEnd));
        }

        return query.offset(from)
                .limit(size)
                .fetch(it -> {
                    EventFullDto event = RecordToEventMapper.map(it);
                    event.setComments(it.get(comments));
                    return event;
                });
    }


    @Override
    public EventFullDto updateEvent(UpdateEventAdminRequest request, Long eventId) {
        Map<Field<?>, Object> updates = new HashMap<>();

        Optional.ofNullable(request.getAnnotation()).ifPresent(v ->
                updates.put(Events.EVENTS.ANNOTATION, v));
        Optional.ofNullable(request.getCategory()).ifPresent(v ->
                updates.put(Events.EVENTS.CATEGORY_ID, v));
        Optional.ofNullable(request.getDescription()).ifPresent(v ->
                updates.put(Events.EVENTS.DESCRIPTION, v));
        Optional.ofNullable(request.getEventDate()).ifPresent(v ->
                updates.put(Events.EVENTS.EVENT_DATE, v));

        Optional.ofNullable(request.getLocation()).ifPresent(v -> dsl.update(Locations.LOCATIONS)
                .set(Locations.LOCATIONS.LAT, v.getLat())
                .set(Locations.LOCATIONS.LON, v.getLon())
                .where(Locations.LOCATIONS.ID.eq(
                        dsl.select(Events.EVENTS.LOCATION_ID)
                                .from(Events.EVENTS)
                                .where(Events.EVENTS.ID.eq(eventId))
                                .fetchOptional()
                                .map(record -> record.into(Long.class))
                                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s does " +
                                        "not exist in the database", eventId)))
                ))
                .execute());

        Optional.ofNullable(request.getPaid()).ifPresent(v ->
                updates.put(Events.EVENTS.PAID, v));
        Optional.ofNullable(request.getParticipantLimit()).ifPresent(v ->
                updates.put(Events.EVENTS.PARTICIPANT_LIMIT, v));
        Optional.ofNullable(request.getRequestModeration()).ifPresent(v ->
                updates.put(Events.EVENTS.REQUEST_MODERATION, v));
        Optional.ofNullable(request.getStateAction()).ifPresent(v -> {
            if (v.equals(StateAction.PUBLISH_EVENT)) {
                updates.put(Events.EVENTS.PUBLISHED_ON, LocalDateTime.now());
                updates.put(Events.EVENTS.STATE, EventState.PUBLISHED);
            } else {
                updates.put(Events.EVENTS.STATE, EventState.CANCELED);
            }
        });
        Optional.ofNullable(request.getTitle()).ifPresent(v ->
                updates.put(Events.EVENTS.TITLE, v));

        if (updates.isEmpty()) {
            throw new IllegalArgumentException("No fields to update");
        }

        dsl.update(Events.EVENTS).set(updates).where(Events.EVENTS.ID.eq(eventId)).execute();

        Field<List<CommentDtoShort>> comments = buildCommentsMultisetField();
        List<SelectFieldOrAsterisk> fields = new ArrayList<>(SELECT_FIELDS);
        fields.add(comments);

        return dsl.select(fields)
                .from(Events.EVENTS)
                .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                .join(Locations.LOCATIONS).on(Locations.LOCATIONS.ID.eq(Events.EVENTS.LOCATION_ID))
                .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                .where(Events.EVENTS.ID.eq(eventId))
                .fetchOptional(it -> {
                    EventFullDto event = RecordToEventMapper.map(it);
                    event.setComments(it.get(comments));
                    return event;
                })
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found",
                        eventId)));
    }

    @Override
    public Record2<LocalDateTime, String> getEventsFieldsForValidation(Long eventId) {
        return dsl.select(Events.EVENTS.CREATED_ON, Events.EVENTS.STATE)
                .from(Events.EVENTS)
                .where(Events.EVENTS.ID.eq(eventId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s does not exist in " +
                        "the database", eventId)));
    }

    @Override
    public List<CommentDto> getComments(List<Long> events, List<Long> users, CommentStatus status, Integer from, Integer size) {
        var query = dsl.select(COMMENTS.ID,
                        COMMENTS.CONTENT,
                        COMMENTS.CREATED,
                        USERS.ID,
                        COMMENTS.STATUS,
                        COMMENTS.EVENT_ID,
                        USERS.NAME)
                .from(COMMENTS)
                .join(USERS).on(USERS.ID.eq(COMMENTS.AUTHOR_ID))
                .where(trueCondition());

        if (events != null && !events.isEmpty()) {
            query = query.and(COMMENTS.EVENT_ID.in(events));
        }

        if (users != null && !users.isEmpty()) {
            query = query.and(COMMENTS.AUTHOR_ID.in(users));
        }

        if (status != null) {
            query = query.and(COMMENTS.STATUS.eq(status.toString()));
        }

        return query.offset(from).limit(size).fetch().map(RecordToCommentMapper::map);
    }

    @Override
    public CommentDto updateComment(Long comId, CommentStatus status) {
        CommentStatus commentStatus = dsl.select(COMMENTS.STATUS)
                .from(COMMENTS)
                .where(COMMENTS.ID.eq(comId))
                .fetchOptional()
                .map(record -> record.into(CommentStatus.class))
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id %s does not exist in " +
                        "the database", comId)));

        if (commentStatus.equals(CommentStatus.REJECTED)) {
            throw new BadRequestException("Comment is already rejected");
        }

        dsl.update(COMMENTS)
                .set(COMMENTS.STATUS, status.toString())
                .where(COMMENTS.ID.eq(comId))
                .execute();

        return dsl.select(COMMENTS.ID,
                        COMMENTS.CONTENT,
                        COMMENTS.CREATED,
                        USERS.ID,
                        COMMENTS.STATUS,
                        COMMENTS.EVENT_ID,
                        USERS.NAME)
                .from(COMMENTS)
                .join(USERS).on(USERS.ID.eq(COMMENTS.AUTHOR_ID))
                .where(COMMENTS.ID.eq(comId))
                .fetchOptional()
                .map(RecordToCommentMapper::map)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with id %s does not exist in " +
                        "the database", comId)));
    }

    private Field<List<CommentDtoShort>> buildCommentsMultisetField() {
        return multiset(
                select(
                        COMMENTS.ID,
                        COMMENTS.CONTENT,
                        COMMENTS.CREATED,
                        COMMENTS.STATUS,
                        COMMENTS.EVENT_ID,
                        USERS.ID,
                        USERS.NAME
                )
                        .from(COMMENTS)
                        .join(USERS).on(USERS.ID.eq(COMMENTS.AUTHOR_ID))
                        .where(COMMENTS.EVENT_ID.eq(Events.EVENTS.ID))
        ).convertFrom(r -> r.map(RecordToShortCommentMapper::map));
    }
}
