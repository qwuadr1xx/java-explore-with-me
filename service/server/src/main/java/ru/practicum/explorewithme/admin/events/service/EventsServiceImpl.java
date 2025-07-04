package ru.practicum.explorewithme.admin.events.service;

import lombok.RequiredArgsConstructor;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.admin.events.repository.EventsRepository;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.EventValidationFields;
import ru.practicum.explorewithme.events.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.events.utils.StateAction;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventsServiceImpl implements EventsService {
    EventsRepository eventsRepository;

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        return eventsRepository.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @Override
    public EventFullDto updateEvent(UpdateEventAdminRequest request, Long eventId) {
        validateEvent(request, eventId);
        return eventsRepository.updateEvent(request, eventId);
    }

    private void validateEvent(UpdateEventAdminRequest request, Long eventId) {
        EventValidationFields eventValidationFields = eventsRepository.getEventsFieldsForValidation(eventId);
        if (request.getEventDate() == null) {
            return;
        }

        if (request.getEventDate().isBefore(eventValidationFields.getCreatedOn().plusDays(1))) {
            throw new IllegalArgumentException("The new start date cannot be within 1 hour after the event is created");
        }

        if (eventValidationFields.getEventState().equals(EventState.PENDING) &&
                (request.getStateAction().equals(StateAction.PUBLISH_EVENT) ||
                        request.getStateAction().equals(StateAction.REJECT_EVENT))) {
            throw new DataAccessException("Cannot publish or reject event in PENDING state");
        }
    }
}
