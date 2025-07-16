package ru.practicum.explorewithme.admin.events.service;

import lombok.RequiredArgsConstructor;
import org.jooq.Record2;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.admin.events.repository.EventsRepository;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.exception.IllegalDateException;
import ru.practicum.explorewithme.jooq.tables.Events;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventsServiceImpl implements EventsService {
    private final EventsRepository eventsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        return eventsRepository.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(UpdateEventAdminRequest request, Long eventId) {
        validateEvent(request, eventId);
        return eventsRepository.updateEvent(request, eventId);
    }

    private void validateEvent(UpdateEventAdminRequest request, Long eventId) {
        Record2<LocalDateTime, String> eventValidationFields = eventsRepository.getEventsFieldsForValidation(eventId);

        if (request.getEventDate() != null &&
                request.getEventDate().isBefore(eventValidationFields.get(Events.EVENTS.CREATED_ON).plusHours(1))) {
            throw new IllegalDateException("The new start date cannot be within 1 hour after the event is created");
        }
        if (request.getStateAction() != null &&
                !eventValidationFields.get(Events.EVENTS.STATE).equals(EventState.PENDING.toString())) {
            throw new DataAccessException("Cannot publish or reject event in PENDING state");
        }
    }
}
