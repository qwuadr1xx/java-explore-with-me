package ru.practicum.explorewithme.publicapi.events.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Categories;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.events.utils.Sort;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Locations;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Users;
import ru.practicum.explorewithme.utils.RecordToEventMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    @Override
    public List<EventFullDto> getEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, Sort sort, Integer from, Integer size) {
        var query = dsl.select(SELECT_FIELDS)
                .from(Events.EVENTS)
                .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                .join(Locations.LOCATIONS).on(Locations.LOCATIONS.ID.eq(Events.EVENTS.LOCATION_ID))
                .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                .where(Events.EVENTS.STATE.eq(EventState.PUBLISHED.toString()));

        if (text != null) {
            query = query.and(Events.EVENTS.TITLE.containsIgnoreCase(text));
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

        List<Record> recordList;

        if (sort != null) {
            if (sort.equals(Sort.EVENT_DATE)) {
                recordList = query.orderBy(Events.EVENTS.EVENT_DATE.asc())
                        .offset(from)
                        .limit(size)
                        .fetch();
            } else {
                recordList = query.orderBy(Events.EVENTS.VIEWS.desc())
                        .offset(from)
                        .limit(size)
                        .fetch();
            }
        } else {
            recordList = query.offset(from)
                    .limit(size)
                    .fetch();
        }

        return recordList.stream().map(RecordToEventMapper::map).toList();
    }

    @Override
    public EventFullDto getEventById(Long eventId) {
        Record record = dsl.select(SELECT_FIELDS)
                .from(Events.EVENTS)
                .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                .join(Locations.LOCATIONS).on(Locations.LOCATIONS.ID.eq(Events.EVENTS.LOCATION_ID))
                .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                .where(Events.EVENTS.ID.eq(eventId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s does not exist in " +
                        "the database", eventId)));

        return RecordToEventMapper.map(record);
    }

    @Override
    public void addView(Long eventId) {
        dsl.update(Events.EVENTS)
                .set(Events.EVENTS.VIEWS, Events.EVENTS.VIEWS.plus(1))
                .where(Events.EVENTS.ID.eq(eventId))
                .execute();
    }
}
