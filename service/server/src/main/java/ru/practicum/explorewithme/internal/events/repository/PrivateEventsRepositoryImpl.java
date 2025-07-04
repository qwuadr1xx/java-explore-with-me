package ru.practicum.explorewithme.internal.events.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.stereotype.Repository;
import ru.explorewithme.jooq.tables.Events;
import ru.explorewithme.jooq.tables.Locations;
import ru.explorewithme.jooq.tables.Users;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.NewEventDto;
import ru.practicum.explorewithme.events.UpdateEventUserRequest;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.events.utils.EventStatus;
import ru.practicum.explorewithme.events.utils.StateAction;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PrivateEventsRepositoryImpl implements EventsRepository {
    private final DSLContext dsl;

    @Override
    public List<EventFullDto> getEvents(Long userId, Integer from, Integer size) {
        return dsl.selectFrom(Events.EVENTS)
                .where(Events.EVENTS.INITIATOR_ID.eq(userId))
                .offset(from)
                .limit(size)
                .fetch()
                .into(EventFullDto.class);
    }

    @Override
    public EventFullDto addEvent(NewEventDto newEventDto, Long userId) {
        findUserById(userId);

        var query = dsl.insertInto(Events.EVENTS)
                .set(Events.EVENTS.INITIATOR_ID, userId)
                .set(Events.EVENTS.ANNOTATION, newEventDto.getAnnotation())
                .set(Events.EVENTS.CATEGORY_ID, newEventDto.getCategory())
                .set(Events.EVENTS.DESCRIPTION, newEventDto.getDescription())
                .set(Events.EVENTS.EVENT_DATE, newEventDto.getEventDate())
                .set(Events.EVENTS.TITLE, newEventDto.getTitle());

        if (newEventDto.getPaid() != null) {
            query = query.set(Events.EVENTS.PAID, newEventDto.getPaid());
        }

        if (newEventDto.getParticipantLimit() != null) {
            query = query.set(Events.EVENTS.PARTICIPANT_LIMIT, newEventDto.getParticipantLimit());
        }

        if (newEventDto.getRequestModeration() != null) {
            query = query.set(Events.EVENTS.REQUEST_MODERATION, newEventDto.getRequestModeration());
        }

        return query.returning()
                .fetchOne()
                .into(EventFullDto.class);
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        return dsl.selectFrom(Events.EVENTS)
                .where(Events.EVENTS.INITIATOR_ID.eq(userId))
                .and(Events.EVENTS.ID.eq(eventId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found for user %d",
                        eventId, userId)))
                .into(EventFullDto.class);
    }

    @Override
    public EventFullDto updateEventById(UpdateEventUserRequest request, Long userId, Long eventId) {
        findUserEvent(userId, eventId);

        Map<Field<?>, Object> updates = new HashMap<>();

        Optional.ofNullable(request.getAnnotation()).ifPresent(v -> updates.put(Events.EVENTS.ANNOTATION, v));
        Optional.ofNullable(request.getDescription()).ifPresent(v -> updates.put(Events.EVENTS.DESCRIPTION, v));
        Optional.ofNullable(request.getEventDate()).ifPresent(v -> updates.put(Events.EVENTS.EVENT_DATE, v));
        Optional.ofNullable(request.getTitle()).ifPresent(v -> updates.put(Events.EVENTS.TITLE, v));
        Optional.ofNullable(request.getPaid()).ifPresent(v -> updates.put(Events.EVENTS.PAID, v));
        Optional.ofNullable(request.getParticipantLimit()).ifPresent(v -> updates.put(Events.EVENTS.PARTICIPANT_LIMIT, v));
        Optional.ofNullable(request.getRequestModeration()).ifPresent(v -> updates.put(Events.EVENTS.REQUEST_MODERATION, v));
        Optional.ofNullable(request.getCategory()).ifPresent(v -> updates.put(Events.EVENTS.CATEGORY_ID, v));

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

        Optional.ofNullable(request.getStateAction()).ifPresent(v -> updates.put(Events.EVENTS.STATE,
                v.equals(StateAction.SEND_TO_REVIEW) ? EventState.PENDING : EventState.CANCELED));

        if (updates.isEmpty()) {
            throw new IllegalArgumentException("No fields to update");
        }
        
        return dsl.update(Events.EVENTS)
                .set(updates)
                .where(Events.EVENTS.ID.eq(eventId))
                .and(Events.EVENTS.INITIATOR_ID.eq(userId))
                .returning()
                .fetchOne()
                .into(EventFullDto.class);
    }

    @Override
    public List<ParticipationRequestDto> getEventsRequests(Long userId, Long eventId) {
        return List.of();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequest request, Long userId, Long eventId) {
        findUserEvent(userId, eventId);

        List<Long> requestIds = request.getRequestIds();
        EventStatus status = request.getStatus();

        if (status == EventStatus.CONFIRMED) {
            dsl.update(ParticipationRequests.PARTICIPATION_REQUESTS)
                    .set(ParticipationRequests.PARTICIPATION_REQUESTS.STATUS, EventStatus.CONFIRMED)
                    .where(ParticipationRequests.PARTICIPATION_REQUESTS.ID.in(requestIds))
                    .execute();
        } else if (status == EventStatus.REJECTED) {
            dsl.update(ParticipationRequests.PARTICIPATION_REQUESTS)
                    .set(ParticipationRequests.PARTICIPATION_REQUESTS.STATUS, EventStatus.REJECTED)
                    .where(ParticipationRequests.PARTICIPATION_REQUESTS.ID.in(requestIds))
                    .execute();
        }

        List<ParticipationRequestDto> confirmedRequests = dsl.selectFrom(ParticipationRequests.PARTICIPATION_REQUESTS)
                .where(ParticipationRequests.PARTICIPATION_REQUESTS.ID.in(requestIds))
                .and(ParticipationRequests.PARTICIPATION_REQUESTS.STATUS.eq(EventStatus.CONFIRMED))
                .fetchInto(ParticipationRequestDto.class);

        List<ParticipationRequestDto> rejectedRequests = dsl.selectFrom(ParticipationRequests.PARTICIPATION_REQUESTS)
                .where(ParticipationRequests.PARTICIPATION_REQUESTS.ID.in(requestIds))
                .and(ParticipationRequests.PARTICIPATION_REQUESTS.STATUS.eq(EventStatus.REJECTED))
                .fetchInto(ParticipationRequestDto.class);

        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }

    private void findUserById(Long userId) {
        dsl.selectFrom(Users.USERS)
                .where(Users.USERS.ID.eq(userId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)));
    }

    private void findUserEvent(Long userId, Long eventId) {
        dsl.selectFrom(Events.EVENTS)
                .where(Events.EVENTS.ID.eq(eventId))
                .and(Events.EVENTS.INITIATOR_ID.eq(userId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found for user %d",
                        eventId, userId)));
    }
}
