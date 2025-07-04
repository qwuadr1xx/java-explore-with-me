package ru.practicum.explorewithme.admin.compilations.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.admin.compilations.service.CompilationsService;
import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.complitations.NewCompilationDto;

@Slf4j
@RequiredArgsConstructor
@RestController("adminCompilationsController")
@RequestMapping("/admin/compilations")
public class CompilationsController {
    CompilationsService compilationsService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("POST /admin/compilations - добавление подборки {}", newCompilationDto);
        return compilationsService.addCompilation(newCompilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("DELETE /admin/compilations/{} - удаление подборки", compId);
        compilationsService.deleteCompilation(compId);
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto updateCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto,
                                         @PathVariable Long compId) {
        log.info("PATCH /admin/compilations/{} - обновление подборки {}", compId, newCompilationDto);
        return compilationsService.updateCompilation(newCompilationDto, compId);
    }
}
