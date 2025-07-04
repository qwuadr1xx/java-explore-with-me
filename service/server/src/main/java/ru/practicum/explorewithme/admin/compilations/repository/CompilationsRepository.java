package ru.practicum.explorewithme.admin.compilations.repository;

import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.complitations.NewCompilationDto;

public interface CompilationsRepository {
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(NewCompilationDto newCompilationDto, Long compId);
}
