package ru.practicum.explorewithme.admin.events.service;

import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.utils.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventsService {
    List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEvent(UpdateEventAdminRequest request, Long eventId);
}
