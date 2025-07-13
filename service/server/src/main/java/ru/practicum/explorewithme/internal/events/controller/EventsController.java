package ru.practicum.explorewithme.internal.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.NewEventDto;
import ru.practicum.explorewithme.events.UpdateEventUserRequest;
import ru.practicum.explorewithme.internal.events.service.EventsService;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.requests.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController("privateEventsController")
@RequestMapping("/users/{userId}/events")
public class EventsController {
    private final EventsService eventsService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEvents(@PathVariable("userId") Long userId,
                                        @RequestParam(required = false, defaultValue = "0") Integer from,
                                        @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET /users/{}/events - Получение событий для userId: {}, from: {}, size: {}", userId, userId, from, size);
        return eventsService.getEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@Valid @RequestBody NewEventDto newEventDto,
                                 @PathVariable("userId") Long userId) {
        log.info("POST /users/{}/events - Добавление нового события", userId);
        return eventsService.addEvent(newEventDto, userId);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventById(@PathVariable("userId") Long userId,
                                     @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{} - Получение события с id: {} для userId: {}", userId, eventId, eventId, userId);
        return eventsService.getEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEventById(@Valid @RequestBody UpdateEventUserRequest request,
                                        @PathVariable("userId") Long userId,
                                        @PathVariable Long eventId) {
        log.info("PATCH /users/{}/events/{} - Обновление события с id: {} для userId: {}", userId, eventId, eventId, userId);
        return eventsService.updateEventById(request, userId, eventId);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getEventsRequests(@PathVariable("userId") Long userId,
                                                           @PathVariable Long eventId) {
        log.info("GET /users/{}/events/{}/requests - Получение запросов на участие в событии с id: {} для userId: {}", userId, eventId, eventId, userId);
        return eventsService.getEventsRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestStatus(@Valid @RequestBody
                                                              EventRequestStatusUpdateRequest request,
                                                              @PathVariable("userId") Long userId,
                                                              @PathVariable Long eventId) {
        log.info("PATCH /users/{}/events/{}/requests - Обновление статуса запросов для события с id: {} для userId: {}", userId, eventId, eventId, userId);
        return eventsService.updateRequestStatus(request, userId, eventId);
    }
}
