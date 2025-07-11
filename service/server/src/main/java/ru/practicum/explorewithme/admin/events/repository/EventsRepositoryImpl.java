package ru.practicum.explorewithme.admin.events.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Categories;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Locations;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.EventValidationFields;
import ru.practicum.explorewithme.events.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.events.utils.StateAction;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Users;
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
        var query = dsl.select(SELECT_FIELDS).from(Events.EVENTS).where(DSL.trueCondition());

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

        return query.limit(size)
                .offset(from)
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

        Optional.ofNullable(request.getLocation()).ifPresent(v -> {
            Long locId = dsl.select(Events.EVENTS.LOCATION_ID)
                    .from(Events.EVENTS)
                    .where(Events.EVENTS.ID.eq(eventId))
                    .fetchOne()
                    .into(Long.class);

            dsl.deleteFrom(Locations.LOCATIONS).where(Locations.LOCATIONS.ID.eq(locId)).execute();

            Long newLocId = dsl.insertInto(Locations.LOCATIONS)
                    .set(Locations.LOCATIONS.LAT, v.getLat())
                    .set(Locations.LOCATIONS.LON, v.getLon())
                    .returning(Locations.LOCATIONS.ID)
                    .fetchOne()
                    .into(Long.class);

            updates.put(Events.EVENTS.LOCATION_ID, newLocId);
        });

        Optional.ofNullable(request.getPaid()).ifPresent(v ->
                updates.put(Events.EVENTS.PAID, v));
        Optional.ofNullable(request.getParticipantLimit()).ifPresent(v ->
                updates.put(Events.EVENTS.PARTICIPANT_LIMIT, v));
        Optional.ofNullable(request.getRequestModeration()).ifPresent(v ->
                updates.put(Events.EVENTS.REQUEST_MODERATION, v));
        Optional.ofNullable(request.getStateAction()).ifPresent(v ->
                updates.put(Events.EVENTS.STATE,
                        v.equals(StateAction.PUBLISH_EVENT) ? EventState.PUBLISHED : EventState.CANCELED));
        Optional.ofNullable(request.getTitle()).ifPresent(v ->
                updates.put(Events.EVENTS.TITLE, v));

        if (updates.isEmpty()) {
            throw new IllegalArgumentException("No fields to update");
        }

        dsl.update(Events.EVENTS).set(updates).execute();

        return dsl.select(SELECT_FIELDS)
                .from(Events.EVENTS)
                .where(Events.EVENTS.ID.eq(eventId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s does not exist in " +
                        "the database", eventId)))
                .into(EventFullDto.class);
    }

    @Override
    public EventValidationFields getEventsFieldsForValidation(Long eventId) {
        return dsl.select(Events.EVENTS.CREATED_ON, Events.EVENTS.STATE)
                .from(Events.EVENTS)
                .where(Events.EVENTS.ID.eq(eventId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s does not exist in " +
                        "the database", eventId)))
                .into(EventValidationFields.class);
    }
}
