package ru.practicum.explorewithme.admin.events.repository;

import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.EventValidationFields;
import ru.practicum.explorewithme.events.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.utils.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventsRepository {
    List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEvent(UpdateEventAdminRequest request, Long eventId);

    EventValidationFields getEventsFieldsForValidation(Long eventId);
}
