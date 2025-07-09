package ru.practicum.explorewithme.publicapi.compilations.service;

import ru.practicum.explorewithme.complitations.CompilationDto;

import java.util.List;

public interface CompilationsService {
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationById(Long compId);
}
