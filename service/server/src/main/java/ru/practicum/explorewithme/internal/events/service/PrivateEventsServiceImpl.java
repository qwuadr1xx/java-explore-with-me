package ru.practicum.explorewithme.internal.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.NewEventDto;
import ru.practicum.explorewithme.events.UpdateEventUserRequest;
import ru.practicum.explorewithme.exception.IllegalDateException;
import ru.practicum.explorewithme.internal.events.repository.EventsRepository;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateEventsServiceImpl implements EventsService {
    private final EventsRepository eventsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getEvents(Long userId, Integer from, Integer size) {
        return eventsRepository.getEvents(userId, from, size);
    }

    @Override
    @Transactional
    public EventFullDto addEvent(NewEventDto newEventDto, Long userId) {
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalDateException("Event date cannot be before 2 hours from now");
        }

        return eventsRepository.addEvent(newEventDto, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(Long userId, Long eventId) {
        return eventsRepository.getEventById(userId, eventId);
    }

    @Override
    @Transactional
    public EventFullDto updateEventById(UpdateEventUserRequest request, Long userId, Long eventId) {
        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new IllegalDateException("Event date cannot be before 2 hours from now");
        }

        return eventsRepository.updateEventById(request, userId, eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventsRequests(Long userId, Long eventId) {
        return eventsRepository.getEventsRequests(userId, eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequest request, Long userId, Long eventId) {
        return eventsRepository.updateRequestStatus(request, userId, eventId);
    }
}
