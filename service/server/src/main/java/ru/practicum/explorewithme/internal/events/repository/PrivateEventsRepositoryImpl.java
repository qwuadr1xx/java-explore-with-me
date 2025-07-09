package ru.practicum.explorewithme.internal.events.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.springframework.stereotype.Repository;
import ru.explorewithme.jooq.tables.Events;
import ru.explorewithme.jooq.tables.Locations;
import ru.explorewithme.jooq.tables.Requests;
import ru.explorewithme.jooq.tables.Users;
import ru.explorewithme.jooq.tables.records.RequestsRecord;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.NewEventDto;
import ru.practicum.explorewithme.events.UpdateEventUserRequest;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.events.utils.EventStatus;
import ru.practicum.explorewithme.events.utils.StateAction;
import ru.practicum.explorewithme.exception.IllegalRequestStatusException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.time.LocalDateTime;
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
                .set(Events.EVENTS.TITLE, newEventDto.getTitle())
                .set(Events.EVENTS.CREATED_ON, LocalDateTime.now());

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
                    .fetchOptional()
                    .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found", eventId)))
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
        return dsl.selectFrom(Requests.REQUESTS)
                .where(Requests.REQUESTS.EVENT_ID.eq(eventId))
                .and(Requests.REQUESTS.REQUESTER.eq(userId))
                .fetchInto(ParticipationRequestDto.class);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequest request,
                                                              Long userId, Long eventId) {
        findUserEvent(userId, eventId);

        List<Long> requestIds = request.getRequestIds();
        EventStatus newStatus = request.getStatus();

        Record3<Integer, Integer, Boolean> recordMap = dsl.select(
                        Events.EVENTS.CONFIRMED_REQUESTS,
                        Events.EVENTS.PARTICIPANT_LIMIT,
                        Events.EVENTS.REQUEST_MODERATION)
                .from(Events.EVENTS)
                .where(Events.EVENTS.ID.eq(eventId))
                .fetchOne();

        Integer confirmedRequestsInEvent = recordMap.get(Events.EVENTS.CONFIRMED_REQUESTS);
        Integer participantLimit = recordMap.get(Events.EVENTS.PARTICIPANT_LIMIT);
        Boolean requestModeration = recordMap.get(Events.EVENTS.REQUEST_MODERATION);

        if (participantLimit != 0) {
            List<Long> firstIds = requestIds.subList(0, Math.min(requestIds.size(),
                    participantLimit - confirmedRequestsInEvent));

            List<Long> lastIds = requestIds.subList(firstIds.size(), requestIds.size());

            Result<RequestsRecord> firstRequestRecords = dsl.selectFrom(Requests.REQUESTS)
                    .where(Requests.REQUESTS.ID.in(firstIds))
                    .fetch();

            Result<RequestsRecord> lastRequestRecords = dsl.selectFrom(Requests.REQUESTS)
                    .where(Requests.REQUESTS.ID.in(lastIds))
                    .fetch();

            if (firstRequestRecords.size() != firstIds.size() || lastRequestRecords.size() != lastIds.size()) {
                throw new NotFoundException("Some requests were not found in the database");
            }

            List<Long> invalidFirstStatusIds = firstRequestRecords.stream()
                    .filter(r -> !"PENDING".equals(r.get(Requests.REQUESTS.STATUS)))
                    .map(r -> r.get(Requests.REQUESTS.ID))
                    .toList();

            List<Long> invalidLastStatusIds = lastRequestRecords.stream()
                    .filter(r -> !"PENDING".equals(r.get(Requests.REQUESTS.STATUS)))
                    .map(r -> r.get(Requests.REQUESTS.ID))
                    .toList();

            if (!invalidFirstStatusIds.isEmpty()) {
                throw new IllegalRequestStatusException("Some requests are not in PENDING status. IDs: " +
                        invalidFirstStatusIds);
            } else if (!invalidLastStatusIds.isEmpty()) {
                throw new IllegalRequestStatusException("Some requests are not in PENDING status. IDs: " +
                        invalidLastStatusIds);
            }

            if (requestModeration) {
                if (newStatus == EventStatus.CONFIRMED) {
                    List<ParticipationRequestDto> confirmedStatusList = dsl.update(Requests.REQUESTS)
                            .set(Requests.REQUESTS.STATUS, "CONFIRMED")
                            .where(Requests.REQUESTS.ID.in(firstIds))
                            .returning()
                            .fetchInto(ParticipationRequestDto.class);

                    List<ParticipationRequestDto> rejectedStatusList = dsl.update(Requests.REQUESTS)
                            .set(Requests.REQUESTS.STATUS, "REJECTED")
                            .where(Requests.REQUESTS.ID.in(lastIds))
                            .returning()
                            .fetchInto(ParticipationRequestDto.class);

                    dsl.update(Events.EVENTS)
                            .set(Events.EVENTS.CONFIRMED_REQUESTS, confirmedRequestsInEvent + firstIds.size())
                            .where(Events.EVENTS.ID.eq(eventId))
                            .execute();

                    return EventRequestStatusUpdateResult.builder()
                            .confirmedRequests(confirmedStatusList)
                            .rejectedRequests(rejectedStatusList)
                            .build();
                } else {
                    List<ParticipationRequestDto> rejectedStatusList = dsl.update(Requests.REQUESTS)
                            .set(Requests.REQUESTS.STATUS, newStatus.toString())
                            .where(Requests.REQUESTS.ID.in(requestIds))
                            .returning()
                            .fetchInto(ParticipationRequestDto.class);

                    return EventRequestStatusUpdateResult.builder()
                            .rejectedRequests(rejectedStatusList)
                            .build();
                }
            } else {
                List<ParticipationRequestDto> confirmedStatusList = dsl.update(Requests.REQUESTS)
                        .set(Requests.REQUESTS.STATUS, "CONFIRMED")
                        .where(Requests.REQUESTS.ID.in(firstIds))
                        .returning()
                        .fetchInto(ParticipationRequestDto.class);

                List<ParticipationRequestDto> rejectedStatusList = dsl.update(Requests.REQUESTS)
                        .set(Requests.REQUESTS.STATUS, "REJECTED")
                        .where(Requests.REQUESTS.ID.in(lastIds))
                        .returning()
                        .fetchInto(ParticipationRequestDto.class);

                dsl.update(Events.EVENTS)
                        .set(Events.EVENTS.CONFIRMED_REQUESTS, confirmedRequestsInEvent + firstIds.size())
                        .where(Events.EVENTS.ID.eq(eventId))
                        .execute();

                return EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(confirmedStatusList)
                        .rejectedRequests(rejectedStatusList)
                        .build();
            }
        } else {
            Result<RequestsRecord> requestRecords = dsl.selectFrom(Requests.REQUESTS)
                    .where(Requests.REQUESTS.ID.in(requestIds))
                    .fetch();

            if (requestRecords.size() != requestIds.size()) {
                throw new NotFoundException("Some requests were not found in the database");
            }

            List<Long> invalidRequestStatusIds = requestRecords.stream()
                    .filter(r -> !"PENDING".equals(r.get(Requests.REQUESTS.STATUS)))
                    .map(r -> r.get(Requests.REQUESTS.ID))
                    .toList();

            if (!invalidRequestStatusIds.isEmpty()) {
                throw new IllegalRequestStatusException("Some requests are not in PENDING status. IDs: " +
                        invalidRequestStatusIds);
            }

            if (requestModeration) {
                if (newStatus == EventStatus.CONFIRMED) {
                    List<ParticipationRequestDto> confirmedStatusList = dsl.update(Requests.REQUESTS)
                            .set(Requests.REQUESTS.STATUS, "CONFIRMED")
                            .where(Requests.REQUESTS.ID.in(requestIds))
                            .returning()
                            .fetchInto(ParticipationRequestDto.class);

                    dsl.update(Events.EVENTS)
                            .set(Events.EVENTS.CONFIRMED_REQUESTS, confirmedRequestsInEvent + requestIds.size())
                            .where(Events.EVENTS.ID.eq(eventId))
                            .execute();

                    return EventRequestStatusUpdateResult.builder()
                            .confirmedRequests(confirmedStatusList)
                            .build();
                } else {
                    List<ParticipationRequestDto> rejectedStatusList = dsl.update(Requests.REQUESTS)
                            .set(Requests.REQUESTS.STATUS, newStatus.toString())
                            .where(Requests.REQUESTS.ID.in(requestIds))
                            .returning()
                            .fetchInto(ParticipationRequestDto.class);

                    return EventRequestStatusUpdateResult.builder()
                            .rejectedRequests(rejectedStatusList)
                            .build();
                }
            } else {
                List<ParticipationRequestDto> confirmedStatusList = dsl.update(Requests.REQUESTS)
                        .set(Requests.REQUESTS.STATUS, "CONFIRMED")
                        .where(Requests.REQUESTS.ID.in(requestIds))
                        .returning()
                        .fetchInto(ParticipationRequestDto.class);

                dsl.update(Events.EVENTS)
                        .set(Events.EVENTS.CONFIRMED_REQUESTS, confirmedRequestsInEvent + requestIds.size())
                        .where(Events.EVENTS.ID.eq(eventId))
                        .execute();

                return EventRequestStatusUpdateResult.builder()
                        .confirmedRequests(confirmedStatusList)
                        .build();
            }
        }
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
