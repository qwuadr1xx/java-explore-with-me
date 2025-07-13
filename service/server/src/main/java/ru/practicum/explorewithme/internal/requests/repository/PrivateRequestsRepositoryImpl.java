package ru.practicum.explorewithme.internal.requests.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record5;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.exception.InvalidStateException;
import ru.practicum.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.jooq.tables.Requests;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PrivateRequestsRepositoryImpl implements RequestsRepository {
    private final DSLContext dsl;

    @Override
    public List<ParticipationRequestDto> getUsersRequest(Long userId) {
        return dsl.selectFrom(Requests.REQUESTS)
                .where(Requests.REQUESTS.REQUESTER.eq(userId))
                .stream().map(requestsRecord -> ParticipationRequestDto.builder()
                        .id(requestsRecord.getId())
                        .requester(requestsRecord.getRequester())
                        .status(requestsRecord.getStatus())
                        .event(requestsRecord.getEventId())
                        .created(requestsRecord.getCreated())
                        .build())
                .toList();
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        if (dsl.selectFrom(Requests.REQUESTS)
                .where(Requests.REQUESTS.REQUESTER.eq(userId)
                        .and(Requests.REQUESTS.EVENT_ID.eq(eventId)))
                .fetchOptional().isPresent()) {
            throw new DataAccessException("You already have a request for this event");
        }

        Record5<Integer, Integer, String, Long, Boolean> record = dsl.select(Events.EVENTS.CONFIRMED_REQUESTS,
                        Events.EVENTS.PARTICIPANT_LIMIT, Events.EVENTS.STATE, Events.EVENTS.INITIATOR_ID,
                        Events.EVENTS.REQUEST_MODERATION)
                .from(Events.EVENTS)
                .where(Events.EVENTS.ID.eq(eventId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));

        if (!record.get(Events.EVENTS.STATE).equals("PUBLISHED")) {
            throw new InvalidStateException("Event with id " + eventId + " is not published, it is " +
                    record.get(Events.EVENTS.STATE));
        } else if (!record.get(Events.EVENTS.PARTICIPANT_LIMIT).equals(0) &&
                record.get(Events.EVENTS.CONFIRMED_REQUESTS).equals(record.get(Events.EVENTS.PARTICIPANT_LIMIT))) {
            throw new DataAccessException("Event with id " + eventId + " is full");
        } else if (record.get(Events.EVENTS.INITIATOR_ID).equals(userId)) {
            throw new DataAccessException("You cannot participate in your own event");
        }

        Map<Field<?>, Object> setMap = Map.of(
                Requests.REQUESTS.REQUESTER, userId,
                Requests.REQUESTS.CREATED, LocalDateTime.now(),
                Requests.REQUESTS.EVENT_ID, eventId
        );

        if (record.get(Events.EVENTS.REQUEST_MODERATION)) {
            return dsl.insertInto(Requests.REQUESTS)
                    .set(setMap)
                    .set(Requests.REQUESTS.STATUS, "PENDING")
                    .returning()
                    .fetchOptional()
                    .orElseThrow(() -> new DataAccessException("Something went wrong"))
                    .map(rec -> new ParticipationRequestDto(
                            rec.get(Requests.REQUESTS.ID),
                            rec.get(Requests.REQUESTS.CREATED),
                            rec.get(Requests.REQUESTS.EVENT_ID),
                            rec.get(Requests.REQUESTS.REQUESTER),
                            rec.get(Requests.REQUESTS.STATUS)
                    ));
        } else {
            dsl.update(Events.EVENTS)
                    .set(Events.EVENTS.CONFIRMED_REQUESTS, Events.EVENTS.CONFIRMED_REQUESTS.plus(1))
                    .where(Events.EVENTS.ID.eq(eventId))
                    .execute();

            return dsl.insertInto(Requests.REQUESTS)
                    .set(setMap)
                    .set(Requests.REQUESTS.STATUS, "CONFIRMED")
                    .returning(Requests.REQUESTS.ID, Requests.REQUESTS.REQUESTER, Requests.REQUESTS.EVENT_ID,
                            Requests.REQUESTS.STATUS, Requests.REQUESTS.CREATED)
                    .fetchOptional()
                    .orElseThrow(() -> new DataAccessException("Something went wrong"))
                    .map(rec -> new ParticipationRequestDto(
                            rec.get(Requests.REQUESTS.ID),
                            rec.get(Requests.REQUESTS.CREATED),
                            rec.get(Requests.REQUESTS.EVENT_ID),
                            rec.get(Requests.REQUESTS.REQUESTER),
                            rec.get(Requests.REQUESTS.STATUS)
                    ));
        }
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        return dsl.update(Requests.REQUESTS)
                .set(Requests.REQUESTS.STATUS, "CANCELED")
                .where(Requests.REQUESTS.ID.eq(requestId))
                .and(Requests.REQUESTS.REQUESTER.eq(userId))
                .returning()
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException("Request with id " + requestId + " not found"))
                .map(rec -> new ParticipationRequestDto(
                        rec.get(Requests.REQUESTS.ID),
                        rec.get(Requests.REQUESTS.CREATED),
                        rec.get(Requests.REQUESTS.EVENT_ID),
                        rec.get(Requests.REQUESTS.REQUESTER),
                        rec.get(Requests.REQUESTS.STATUS)
                ));
    }
}
