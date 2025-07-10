package ru.practicum.explorewithme.publicapi.events.repository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.HitDtoIn;
import ru.practicum.explorewithme.client.StatClient;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.events.utils.Sort;
import ru.practicum.explorewithme.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PublicEventsRepositoryImpl implements EventsRepository {
    private final DSLContext dsl;
    private final StatClient statClient;

    @Override
    public List<EventFullDto> getEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, Sort sort, Integer from, Integer size, String ipAddress) {
        var query = dsl.selectFrom(Events.EVENTS).where(Events.EVENTS.STATE.eq(EventState.PUBLISHED.toString()));

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

        List<EventFullDto> list;

        if (sort != null) {
            if (sort.equals(Sort.EVENT_DATE)) {
                list = query.orderBy(Events.EVENTS.EVENT_DATE.asc())
                        .offset(from)
                        .limit(size)
                        .fetchInto(EventFullDto.class);
            } else {
                list = query.orderBy(Events.EVENTS.VIEWS.desc())
                        .offset(from)
                        .limit(size)
                        .fetchInto(EventFullDto.class);
            }
        } else {
            list = query.offset(from)
                    .limit(size)
                    .fetchInto(EventFullDto.class);
        }

        if (list.isEmpty()) {
            return list;
        }


        List<Long> idsList = list.stream().map(EventFullDto::getId).toList();

        dsl.update(Events.EVENTS)
                .set(Events.EVENTS.VIEWS, Events.EVENTS.VIEWS.plus(1))
                .where(Events.EVENTS.ID.in(idsList))
                .execute();

        idsList.forEach(id -> statClient.saveHit(HitDtoIn.builder()
                .app("ewm-main-service")
                .uri("/events/" + id)
                .timestamp(LocalDateTime.now())
                .ip(ipAddress)
                .build()));

        return list;
    }

    @Override
    public EventFullDto getEventById(Long eventId, String ipAddress) {
        EventFullDto eventFullDto = dsl.selectFrom(Events.EVENTS)
                .where(Events.EVENTS.ID.eq(eventId))
                .fetchOptional()
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s does not exist in " +
                        "the database", eventId)))
                .into(EventFullDto.class);

        dsl.update(Events.EVENTS)
                .set(Events.EVENTS.VIEWS, Events.EVENTS.VIEWS.plus(1))
                .where(Events.EVENTS.ID.eq(eventId))
                .execute();

        statClient.saveHit(HitDtoIn.builder()
                .app("ewm-main-service")
                .uri("/events/" + eventId)
                .timestamp(LocalDateTime.now())
                .ip(ipAddress)
                .build());

        return eventFullDto;
    }
}
