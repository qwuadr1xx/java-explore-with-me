package ru.practicum.explorewithme.admin.compilations.service;

import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.complitations.NewCompilationDto;

public interface CompilationsService {
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(NewCompilationDto newCompilationDto, Long compId);
}
