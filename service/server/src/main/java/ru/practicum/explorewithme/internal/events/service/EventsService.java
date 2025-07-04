package ru.practicum.explorewithme.internal.events.service;

import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.NewEventDto;
import ru.practicum.explorewithme.events.UpdateEventUserRequest;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.util.List;

public interface EventsService {

    List<EventFullDto> getEvents(Long userId, Integer from, Integer size);

    EventFullDto addEvent(NewEventDto newEventDto, Long userId);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEventById(UpdateEventUserRequest request, Long userId, Long eventId);

    List<ParticipationRequestDto> getEventsRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestStatus(EventRequestStatusUpdateRequest request, Long userId, Long eventId);
}
