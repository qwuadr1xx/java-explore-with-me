package ru.practicum.explorewithme.internal.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.NewEventDto;
import ru.practicum.explorewithme.events.UpdateEventUserRequest;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateEventsServiceImpl implements EventsService {
    @Override
    public List<EventFullDto> getEvents(Long userId, Integer from, Integer size) {
        return List.of();
    }

    @Override
    public EventFullDto addEvent(NewEventDto newEventDto, Long userId) {
        if (newEventDto.getEventDate())
        return null;
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        return null;
    }

    @Override
    public EventFullDto updateEventById(UpdateEventUserRequest request, Long userId, Long eventId) {
        return null;
    }

    @Override
    public List<ParticipationRequestDto> getEventsRequests(Long userId, Long eventId) {
        return List.of();
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequest request, Long userId, Long eventId) {
        return null;
    }
}
