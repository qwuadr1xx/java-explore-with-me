package ru.practicum.explorewithme.admin.events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.admin.events.service.EventsService;
import ru.practicum.explorewithme.comments.CommentDto;
import ru.practicum.explorewithme.comments.util.CommentStatus;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.utils.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/events")
public class AdminEventsController {
    private final EventsService eventsService;
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) List<EventState> states,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN)
                                        LocalDateTime rangeStart,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN)
                                        LocalDateTime rangeEnd,
                                        @RequestParam(required = false, defaultValue = "0") Integer from,
                                        @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET /admin/events - Получение событий с фильтрами: users={}, states={}, categories={}, " +
                        "rangeStart={}, rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return eventsService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@Valid @RequestBody UpdateEventAdminRequest request,
                                    @PathVariable Long eventId) {
        log.info("PATCH /admin/events/{} - Обновление события с запросом: {}", eventId, request);
        return eventsService.updateEvent(request, eventId);
    }

    @GetMapping("/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getComments(@RequestParam(required = false) List<Long> events,
                                        @RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) CommentStatus status,
                                        @RequestParam(required = false, defaultValue = "0") Integer from,
                                        @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET /admin/events/comments - Получение комментариев с фильтрами: events={}, users={}, status={}, from={}, size={}",
                events, users, status, from, size);
        return eventsService.getComments(events, users, status, from, size);
    }

    @PatchMapping("/comments/{comId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto updateComment(@PathVariable Long comId,
                                    @RequestParam CommentStatus status) {
        log.info("PATCH /admin/events/comments/{} - Изменение статуса комментария на {}", comId, status);
        return eventsService.updateComment(comId, status);
    }
}
