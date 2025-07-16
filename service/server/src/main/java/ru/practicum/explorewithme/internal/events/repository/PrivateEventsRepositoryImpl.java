package ru.practicum.explorewithme.internal.events.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.events.utils.RequestStatus;
import ru.practicum.explorewithme.exception.InvalidStateException;
import ru.practicum.explorewithme.jooq.tables.*;
import ru.practicum.explorewithme.jooq.tables.records.RequestsRecord;
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
import ru.practicum.explorewithme.users.UserDto;
import ru.practicum.explorewithme.users.UserShortDto;
import ru.practicum.explorewithme.utils.RecordToEventMapper;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class PrivateEventsRepositoryImpl implements EventsRepository {
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
    public List<EventFullDto> getEvents(Long userId, Integer from, Integer size) {
        return dsl.select(SELECT_FIELDS)
                .from(Events.EVENTS)
                .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                .join(Locations.LOCATIONS).on(Locations.LOCATIONS.ID.eq(Events.EVENTS.LOCATION_ID))
                .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                .where(Events.EVENTS.INITIATOR_ID.eq(userId))
                .offset(from)
                .limit(size)
                .fetch()
                .stream().map(RecordToEventMapper::map).toList();
    }

    @Override
    public EventFullDto addEvent(NewEventDto newEventDto, Long userId) {
        UserDto user = findUserById(userId);

        Long locId = dsl.insertInto(Locations.LOCATIONS)
                .set(Locations.LOCATIONS.LAT, newEventDto.getLocation().getLat())
                .set(Locations.LOCATIONS.LON, newEventDto.getLocation().getLon())
                .returningResult(Locations.LOCATIONS.ID)
                .fetchOptional()
                .orElseThrow(() -> new DataAccessException("Problem with inserting location"))
                .into(Long.class);

        var query = dsl.insertInto(Events.EVENTS)
                .set(Events.EVENTS.INITIATOR_ID, userId)
                .set(Events.EVENTS.ANNOTATION, newEventDto.getAnnotation())
                .set(Events.EVENTS.CATEGORY_ID, newEventDto.getCategory())
                .set(Events.EVENTS.DESCRIPTION, newEventDto.getDescription())
                .set(Events.EVENTS.EVENT_DATE, newEventDto.getEventDate())
                .set(Events.EVENTS.TITLE, newEventDto.getTitle())
                .set(Events.EVENTS.CREATED_ON, LocalDateTime.now())
                .set(Events.EVENTS.LOCATION_ID, locId)
                .set(Events.EVENTS.STATE, EventState.PENDING.toString());

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
                .into(EventFullDto.class).toBuilder()
                .location(newEventDto.getLocation())
                .category(dsl.select(Categories.CATEGORIES.ID, Categories.CATEGORIES.NAME)
                                .from(Categories.CATEGORIES)
                                .where(Categories.CATEGORIES.ID.eq(newEventDto.getCategory()))
                                .fetchOneInto(CategoryDto.class))
                .initiator(new UserShortDto(user.getId(), user.getName()))
                .build();
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        return RecordToEventMapper.map(dsl.select(SELECT_FIELDS)
                .from(Events.EVENTS)
                .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                .join(Locations.LOCATIONS).on(Locations.LOCATIONS.ID.eq(Events.EVENTS.LOCATION_ID))
                .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                .where(Events.EVENTS.INITIATOR_ID.eq(userId))
                .and(Events.EVENTS.ID.eq(eventId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found for user %d",
                        eventId, userId))));
    }

    @Override
    public EventFullDto updateEventById(UpdateEventUserRequest request, Long userId, Long eventId) {
        String state = findUserEvent(userId, eventId);

        if (state.equals(EventState.PUBLISHED.toString())) {
            throw new InvalidStateException(String.format("Event with id %d is in %s state", eventId, state));
        }

        Map<Field<?>, Object> updates = new HashMap<>();

        Optional.ofNullable(request.getAnnotation()).ifPresent(v -> updates.put(Events.EVENTS.ANNOTATION, v));
        Optional.ofNullable(request.getDescription()).ifPresent(v -> updates.put(Events.EVENTS.DESCRIPTION, v));
        Optional.ofNullable(request.getEventDate()).ifPresent(v -> updates.put(Events.EVENTS.EVENT_DATE, v));
        Optional.ofNullable(request.getTitle()).ifPresent(v -> updates.put(Events.EVENTS.TITLE, v));
        Optional.ofNullable(request.getPaid()).ifPresent(v -> updates.put(Events.EVENTS.PAID, v));
        Optional.ofNullable(request.getParticipantLimit()).ifPresent(v -> updates.put(Events.EVENTS.PARTICIPANT_LIMIT, v));
        Optional.ofNullable(request.getRequestModeration()).ifPresent(v -> updates.put(Events.EVENTS.REQUEST_MODERATION, v));
        Optional.ofNullable(request.getCategory()).ifPresent(v -> updates.put(Events.EVENTS.CATEGORY_ID, v));

        Optional.ofNullable(request.getLocation()).ifPresent(v -> dsl.update(Locations.LOCATIONS)
                .set(Locations.LOCATIONS.LAT, v.getLat())
                .set(Locations.LOCATIONS.LON, v.getLon())
                .where(Locations.LOCATIONS.ID.eq(
                        dsl.select(Events.EVENTS.LOCATION_ID)
                                .from(Events.EVENTS)
                                .where(Events.EVENTS.ID.eq(eventId))
                                .fetchOptional()
                                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found", eventId)))
                                .into(Long.class)
                ))
                .execute());

        Optional.ofNullable(request.getStateAction()).ifPresent(v -> updates.put(Events.EVENTS.STATE,
                v.equals(StateAction.SEND_TO_REVIEW) ? EventState.PENDING : EventState.CANCELED));

        if (updates.isEmpty()) {
            throw new IllegalArgumentException("No fields to update");
        }

        dsl.update(Events.EVENTS)
                .set(updates)
                .where(Events.EVENTS.ID.eq(eventId))
                .execute();

        return RecordToEventMapper.map(dsl.select(SELECT_FIELDS)
                .from(Events.EVENTS)
                .join(Categories.CATEGORIES).on(Categories.CATEGORIES.ID.eq(Events.EVENTS.CATEGORY_ID))
                .join(Locations.LOCATIONS).on(Locations.LOCATIONS.ID.eq(Events.EVENTS.LOCATION_ID))
                .join(Users.USERS).on(Users.USERS.ID.eq(Events.EVENTS.INITIATOR_ID))
                .where(Events.EVENTS.INITIATOR_ID.eq(userId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found for user %d",
                        eventId, userId))));
    }

    @Override
    public List<ParticipationRequestDto> getEventsRequests(Long userId, Long eventId) {
        return dsl.select(Requests.REQUESTS.ID, Requests.REQUESTS.REQUESTER, Requests.REQUESTS.STATUS,
                        Requests.REQUESTS.EVENT_ID, Requests.REQUESTS.CREATED)
                .from(Requests.REQUESTS)
                .join(Events.EVENTS).on(Events.EVENTS.INITIATOR_ID.eq(userId))
                .where(Requests.REQUESTS.EVENT_ID.eq(eventId))
                .stream().map(requestsRecord -> ParticipationRequestDto.builder()
                        .id(requestsRecord.getValue(Requests.REQUESTS.ID))
                        .requester(requestsRecord.getValue(Requests.REQUESTS.REQUESTER))
                        .status(requestsRecord.getValue(Requests.REQUESTS.STATUS))
                        .event(requestsRecord.getValue(Requests.REQUESTS.EVENT_ID))
                        .created(requestsRecord.getValue(Requests.REQUESTS.CREATED))
                        .build())
                .toList();
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
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found for user %d", eventId, userId)));

        Integer confirmedRequestsInEvent = recordMap.get(Events.EVENTS.CONFIRMED_REQUESTS);
        Integer participantLimit = recordMap.get(Events.EVENTS.PARTICIPANT_LIMIT);
        Boolean requestModeration = recordMap.get(Events.EVENTS.REQUEST_MODERATION);

        if (confirmedRequestsInEvent.equals(participantLimit)) {
            throw new DataAccessException("Event is full. No more requests can be added");
        }

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
                    .filter(r -> !RequestStatus.PENDING.toString().equals(r.get(Requests.REQUESTS.STATUS)))
                    .map(r -> r.get(Requests.REQUESTS.ID))
                    .toList();

            List<Long> invalidLastStatusIds = lastRequestRecords.stream()
                    .filter(r -> !RequestStatus.PENDING.toString().equals(r.get(Requests.REQUESTS.STATUS)))
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
                            .set(Requests.REQUESTS.STATUS, RequestStatus.CONFIRMED.toString())
                            .where(Requests.REQUESTS.ID.in(firstIds))
                            .returning()
                            .stream().map(requestsRecord -> ParticipationRequestDto.builder()
                                    .id(requestsRecord.getId())
                                    .requester(requestsRecord.getRequester())
                                    .status(requestsRecord.getStatus())
                                    .event(requestsRecord.getEventId())
                                    .created(requestsRecord.getCreated())
                                    .build())
                            .toList();

                    List<ParticipationRequestDto> rejectedStatusList = dsl.update(Requests.REQUESTS)
                            .set(Requests.REQUESTS.STATUS, RequestStatus.REJECTED.toString())
                            .where(Requests.REQUESTS.ID.in(lastIds))
                            .returning()
                            .stream().map(requestsRecord -> ParticipationRequestDto.builder()
                                    .id(requestsRecord.getId())
                                    .requester(requestsRecord.getRequester())
                                    .status(requestsRecord.getStatus())
                                    .event(requestsRecord.getEventId())
                                    .created(requestsRecord.getCreated())
                                    .build())
                            .toList();

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
                            .stream().map(requestsRecord -> ParticipationRequestDto.builder()
                                    .id(requestsRecord.getId())
                                    .requester(requestsRecord.getRequester())
                                    .status(requestsRecord.getStatus())
                                    .event(requestsRecord.getEventId())
                                    .created(requestsRecord.getCreated())
                                    .build())
                            .toList();

                    return EventRequestStatusUpdateResult.builder()
                            .rejectedRequests(rejectedStatusList)
                            .build();
                }
            } else {
                List<ParticipationRequestDto> confirmedStatusList = dsl.update(Requests.REQUESTS)
                        .set(Requests.REQUESTS.STATUS, RequestStatus.CONFIRMED.toString())
                        .where(Requests.REQUESTS.ID.in(firstIds))
                        .returning()
                        .stream().map(requestsRecord -> ParticipationRequestDto.builder()
                                .id(requestsRecord.getId())
                                .requester(requestsRecord.getRequester())
                                .status(requestsRecord.getStatus())
                                .event(requestsRecord.getEventId())
                                .created(requestsRecord.getCreated())
                                .build())
                        .toList();

                List<ParticipationRequestDto> rejectedStatusList = dsl.update(Requests.REQUESTS)
                        .set(Requests.REQUESTS.STATUS, RequestStatus.REJECTED.toString())
                        .where(Requests.REQUESTS.ID.in(lastIds))
                        .returning()
                        .stream().map(requestsRecord -> ParticipationRequestDto.builder()
                                .id(requestsRecord.getId())
                                .requester(requestsRecord.getRequester())
                                .status(requestsRecord.getStatus())
                                .event(requestsRecord.getEventId())
                                .created(requestsRecord.getCreated())
                                .build())
                        .toList();

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
                    .filter(r -> !RequestStatus.PENDING.toString().equals(r.get(Requests.REQUESTS.STATUS)))
                    .map(r -> r.get(Requests.REQUESTS.ID))
                    .toList();

            if (!invalidRequestStatusIds.isEmpty()) {
                throw new IllegalRequestStatusException("Some requests are not in PENDING status. IDs: " +
                        invalidRequestStatusIds);
            }

            if (requestModeration) {
                if (newStatus == EventStatus.CONFIRMED) {
                    List<ParticipationRequestDto> confirmedStatusList = dsl.update(Requests.REQUESTS)
                            .set(Requests.REQUESTS.STATUS, RequestStatus.CONFIRMED.toString())
                            .where(Requests.REQUESTS.ID.in(requestIds))
                            .returning()
                            .stream().map(requestsRecord -> ParticipationRequestDto.builder()
                                    .id(requestsRecord.getId())
                                    .requester(requestsRecord.getRequester())
                                    .status(requestsRecord.getStatus())
                                    .event(requestsRecord.getEventId())
                                    .created(requestsRecord.getCreated())
                                    .build())
                            .toList();

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
                            .stream().map(requestsRecord -> ParticipationRequestDto.builder()
                                    .id(requestsRecord.getId())
                                    .requester(requestsRecord.getRequester())
                                    .status(requestsRecord.getStatus())
                                    .event(requestsRecord.getEventId())
                                    .created(requestsRecord.getCreated())
                                    .build())
                            .toList();

                    return EventRequestStatusUpdateResult.builder()
                            .rejectedRequests(rejectedStatusList)
                            .build();
                }
            } else {
                List<ParticipationRequestDto> confirmedStatusList = dsl.update(Requests.REQUESTS)
                        .set(Requests.REQUESTS.STATUS, RequestStatus.CONFIRMED.toString())
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

    private UserDto findUserById(Long userId) {
        return dsl.selectFrom(Users.USERS)
                .where(Users.USERS.ID.eq(userId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("User with id %d not found", userId)))
                .into(UserDto.class);
    }

    private String findUserEvent(Long userId, Long eventId) {
        return dsl.select(Events.EVENTS.STATE)
                .from(Events.EVENTS)
                .where(Events.EVENTS.ID.eq(eventId))
                .and(Events.EVENTS.INITIATOR_ID.eq(userId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %d not found for user %d",
                        eventId, userId)))
                .getValue(Events.EVENTS.STATE);
    }
}
