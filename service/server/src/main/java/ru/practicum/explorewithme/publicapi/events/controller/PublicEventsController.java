package ru.practicum.explorewithme.publicapi.events.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.utils.Sort;
import ru.practicum.explorewithme.publicapi.events.service.EventsService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/events")
public class PublicEventsController {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final EventsService eventsService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEvents(@RequestParam(required = false) String text,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) Boolean paid,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN)
                                        LocalDateTime rangeStart,
                                        @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN)
                                        LocalDateTime rangeEnd,
                                        @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                        @RequestParam(required = false) Sort sort,
                                        @RequestParam(required = false, defaultValue = "0") Integer from,
                                        @RequestParam(required = false, defaultValue = "10") Integer size,
                                        HttpServletRequest request) {
        log.info("GET /events - Получение событий: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, " +
                        "onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        return eventsService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size,
                request.getRemoteAddr());
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventById(@PathVariable Long eventId, HttpServletRequest request) {
        log.info("GET /events/{} - Получение события по ID", eventId);
        return eventsService.getEventById(eventId, request.getRemoteAddr());
    }
}
