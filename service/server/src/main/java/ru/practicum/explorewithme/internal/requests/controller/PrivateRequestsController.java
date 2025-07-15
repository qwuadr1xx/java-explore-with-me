package ru.practicum.explorewithme.internal.requests.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.internal.requests.service.RequestsService;
import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/requests")
public class PrivateRequestsController {
    private final RequestsService requestsService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getUsersRequest(@PathVariable Long userId) {
        log.info("GET /users/{}/requests - Получение запросов пользователя", userId);
        return requestsService.getUsersRequest(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable Long userId,
                                              @RequestParam Long eventId) {
        log.info("POST /users/{}/requests - Добавление запроса на событие: {}", userId, eventId);
        return requestsService.addRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel - Отмена запроса пользователя", userId, requestId);
        return requestsService.cancelRequest(userId, requestId);
    }
}
