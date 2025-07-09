package ru.practicum.explorewithme.publicapi.compilations.repository;

import ru.practicum.explorewithme.complitations.CompilationDto;

import java.util.List;

public interface CompilationsRepository {
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilationById(Long compId);
}
