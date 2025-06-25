package ru.practicum.explorewithme.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.HitDtoIn;
import ru.practicum.explorewithme.ViewStats;
import ru.practicum.explorewithme.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class StatController {
    private final StatService statService;
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getViewStats(@RequestParam(name = "start") @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime start,
                                                        @RequestParam(name = "end") @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime end,
                                                        @RequestParam(required = false) List<String> uris,
                                                        @RequestParam(required = false, defaultValue = "false") Boolean unique) {
        log.info("GET /stats - получение статистики в период с {} по {}", start, end);

        return ResponseEntity.ok(statService.getStats(start, end, uris, unique));
    }

    @PostMapping("/hit")
    public void addHit(@Valid @RequestBody final HitDtoIn hitDtoIn) {
        log.info("POST /hit - создание посещения {}", hitDtoIn);

        statService.addHit(hitDtoIn);
    }
}
