package ru.practicum.explorewithme.internal.requests.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record4;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Requests;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PrivateRequestsRepositoryImpl implements RequestsRepository {
    private final DSLContext dsl;

    @Override
    public List<ParticipationRequestDto> getUsersRequest(Long userId) {
        return dsl.selectFrom(Requests.REQUESTS)
                .where(Requests.REQUESTS.REQUESTER.eq(userId))
                .fetchInto(ParticipationRequestDto.class);
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        Record4<Integer, Integer, String, Long> record = dsl.select(Events.EVENTS.CONFIRMED_REQUESTS, Events.EVENTS.PARTICIPANT_LIMIT,
                Events.EVENTS.STATE, Events.EVENTS.INITIATOR_ID)
                .from(Events.EVENTS)
                .where(Events.EVENTS.ID.eq(eventId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException("Event with id " + eventId + " not found"));

        if (!record.get(Events.EVENTS.STATE).equals("PUBLISHED")) {
            throw new IllegalStateException("Event with id " + eventId + " is not published, it is " +
                    record.get(Events.EVENTS.STATE));
        } else if (record.get(Events.EVENTS.CONFIRMED_REQUESTS).equals(record.get(Events.EVENTS.PARTICIPANT_LIMIT))) {
            throw new DataAccessException("Event with id " + eventId + " is full");
        } else if (record.get(Events.EVENTS.INITIATOR_ID).equals(userId)) {
            throw new DataAccessException("You cannot participate in your own event");
        }

        return dsl.insertInto(Requests.REQUESTS)
                .set(Requests.REQUESTS.REQUESTER, userId)
                .set(Requests.REQUESTS.STATUS, "PENDING")
                .set(Requests.REQUESTS.CREATED, LocalDateTime.now())
                .set(Requests.REQUESTS.EVENT_ID, eventId)
                .returning()
                .fetchOne()
                .into(ParticipationRequestDto.class);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        return dsl.update(Requests.REQUESTS)
                .set(Requests.REQUESTS.STATUS, "CANCELLED")
                .where(Requests.REQUESTS.ID.eq(requestId))
                .and(Requests.REQUESTS.REQUESTER.eq(userId))
                .returning()
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException("Request with id " + requestId + " not found"))
                .into(ParticipationRequestDto.class);
    }
}
