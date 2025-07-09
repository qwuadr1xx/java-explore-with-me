package ru.practicum.explorewithme.publicapi.events.repository;

import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.utils.Sort;

import java.time.LocalDateTime;
import java.util.List;

public interface EventsRepository {
    List<EventFullDto> getEvents(String text,
                                 List<Long> categories,
                                 Boolean paid,
                                 LocalDateTime rangeStart,
                                 LocalDateTime rangeEnd,
                                 Boolean onlyAvailable,
                                 Sort sort,
                                 Integer from,
                                 Integer size,
                                 String ipAddress);

    EventFullDto getEventById(Long eventId, String ipAddress);
}
