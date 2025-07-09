package ru.practicum.explorewithme.publicapi.compilations.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.publicapi.compilations.service.CompilationsService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController("publicCompilationsController")
@RequestMapping("/compilations")
public class CompilationsController {
    private final CompilationsService compilationsService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(required = false, defaultValue = "0") Integer from,
                                                @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("GET /compilations - Получение подборок: from={}, size={}", from, size);
        return compilationsService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto getCompilationById(@PathVariable Long compId) {
        log.info("GET /compilations/{} - Получение подборки по идентификатору", compId);
        return compilationsService.getCompilationById(compId);
    }
}
