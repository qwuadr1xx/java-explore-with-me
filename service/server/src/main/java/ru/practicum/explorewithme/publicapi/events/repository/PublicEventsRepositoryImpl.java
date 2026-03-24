package ru.practicum.explorewithme.publicapi.events.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectFieldOrAsterisk;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.client.StatClient;
import ru.practicum.explorewithme.comments.CommentDtoShort;
import ru.practicum.explorewithme.comments.util.CommentStatus;
import ru.practicum.explorewithme.jooq.tables.Categories;
import ru.practicum.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.events.utils.Sort;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.jooq.tables.Locations;
import ru.practicum.explorewithme.jooq.tables.Users;
import ru.practicum.explorewithme.utils.RecordToEventMapper;
import ru.practicum.explorewithme.utils.RecordToShortCommentMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;
import static ru.practicum.explorewithme.jooq.Tables.COMMENTS;
import static ru.practicum.explorewithme.jooq.Tables.USERS;

@Repository
@RequiredArgsConstructor
public class PublicEventsRepositoryImpl implements EventsRepository {
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

    private final DSLContext dsl;
    private final StatClient statClient;

    @Override
    public List<EventFullDto> getEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, Sort sort, Integer from, Integer size) {
        Field<List<CommentDtoShort>> comments = buildCommentsMultisetField();
        List<SelectFieldOrAsterisk> fields = new ArrayList<>(SELECT_FIELDS);
        fields.add(comments);

        var query = dsl.select(fields)
                .from(Events.EVENTS)
                .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                .join(Locations.LOCATIONS).on(Locations.LOCATIONS.ID.eq(Events.EVENTS.LOCATION_ID))
                .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                .where(Events.EVENTS.STATE.eq(EventState.PUBLISHED.toString()));

        if (text != null) {
            query = query.and(Events.EVENTS.TITLE.containsIgnoreCase(text))
                    .or(Events.EVENTS.ANNOTATION.containsIgnoreCase(text));
        }

        if (categories != null && !categories.isEmpty()) {
            query = query.and(Events.EVENTS.CATEGORY_ID.in(categories));
        }

        if (paid != null) {
            query = query.and(Events.EVENTS.PAID.eq(paid));
        }

        if (rangeEnd != null) {
            query = query.and(Events.EVENTS.EVENT_DATE.between(rangeStart, rangeEnd));
        } else {
            query = query.and(Events.EVENTS.EVENT_DATE.greaterOrEqual(rangeStart));
        }

        if (onlyAvailable != null) {
            if (onlyAvailable) {
                query = query
                        .and(Events.EVENTS.PARTICIPANT_LIMIT.ne(0))
                        .and(Events.EVENTS.PARTICIPANT_LIMIT.greaterThan(Events.EVENTS.CONFIRMED_REQUESTS));

            }
        }

        List<EventFullDto> recordList;

        if (sort != null) {
            if (sort.equals(Sort.EVENT_DATE)) {
                recordList = query.orderBy(Events.EVENTS.EVENT_DATE.asc())
                        .offset(from)
                        .limit(size)
                        .fetch(it -> {
                            EventFullDto event = RecordToEventMapper.map(it);
                            event.setComments(it.get(comments));
                            return event;
                        });
            } else {
                recordList = query.orderBy(Events.EVENTS.VIEWS.desc())
                        .offset(from)
                        .limit(size)
                        .fetch(it -> {
                            EventFullDto event = RecordToEventMapper.map(it);
                            event.setComments(it.get(comments));
                            return event;
                        });
            }
        } else {
            recordList = query.offset(from)
                    .limit(size)
                    .fetch(it -> {
                        EventFullDto event = RecordToEventMapper.map(it);
                        event.setComments(it.get(comments));
                        return event;
                    });
        }
        if (!recordList.isEmpty()) {
            recordList.stream().map(EventFullDto::getId).forEach(id -> dsl.update(Events.EVENTS)
                    .set(Events.EVENTS.VIEWS, getStats(id))
                    .where(Events.EVENTS.ID.eq(id))
                    .execute());
        }
        return recordList;
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Field<List<CommentDtoShort>> comments = buildCommentsMultisetField();
        List<SelectFieldOrAsterisk> fields = new ArrayList<>(SELECT_FIELDS);
        fields.add(comments);

        EventFullDto eventFullDto = dsl.select(fields)
                .from(Events.EVENTS)
                .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                .join(Locations.LOCATIONS).on(Locations.LOCATIONS.ID.eq(Events.EVENTS.LOCATION_ID))
                .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                .where(Events.EVENTS.ID.eq(eventId))
                .and(Events.EVENTS.STATE.eq(EventState.PUBLISHED.toString()))
                .fetchOptional(it -> {
                    EventFullDto event = RecordToEventMapper.map(it);
                    event.setComments(it.get(comments));
                    return event;
                })
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found",
                        eventId)));

        dsl.update(Events.EVENTS)
                .set(Events.EVENTS.VIEWS, getStats(eventId))
                .where(Events.EVENTS.ID.eq(eventId))
                .execute();

        return eventFullDto;
    }

    private Long getStats(Long eventId) {
        var statsResponse = statClient.getStats(LocalDateTime.of(1970, 1, 1, 0, 0),
                LocalDateTime.of(2099, 12, 31, 23, 59),
                List.of("/events/" + eventId.toString()), true);

        if (statsResponse.getBody() == null || statsResponse.getBody().isEmpty()) {
            return 0L;
        }

        return statsResponse.getBody().getFirst().getHits();
    }

    private Field<List<CommentDtoShort>> buildCommentsMultisetField() {
        return multiset(
                select(
                        COMMENTS.ID,
                        COMMENTS.CONTENT,
                        COMMENTS.CREATED,
                        USERS.ID,
                        USERS.NAME
                )
                        .from(COMMENTS)
                        .join(USERS).on(USERS.ID.eq(COMMENTS.AUTHOR_ID))
                        .where(COMMENTS.EVENT_ID.eq(Events.EVENTS.ID))
                        .and(COMMENTS.STATUS.eq(CommentStatus.APPROVED.toString()))
        ).convertFrom(r -> r.map(RecordToShortCommentMapper::map));
    }
}
