package ru.practicum.explorewithme.admin.events.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.jooq.tables.Categories;
import ru.practicum.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.jooq.tables.Locations;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.events.utils.StateAction;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.jooq.tables.Users;
import ru.practicum.explorewithme.utils.RecordToEventMapper;

import java.time.LocalDateTime;
import java.util.*;

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
        var query = dsl.select(SELECT_FIELDS)
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
                .stream().map(RecordToEventMapper::map).toList();
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

        return RecordToEventMapper.map(dsl.select(SELECT_FIELDS)
                .from(Events.EVENTS)
                .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                .join(Locations.LOCATIONS).on(Locations.LOCATIONS.ID.eq(Events.EVENTS.LOCATION_ID))
                .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                .where(Events.EVENTS.ID.eq(eventId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s does not exist in " +
                        "the database", eventId))));
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
}
