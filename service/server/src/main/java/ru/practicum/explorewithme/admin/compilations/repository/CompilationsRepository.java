package ru.practicum.explorewithme.admin.compilations.repository;

import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.complitations.NewCompilationDto;
import ru.practicum.explorewithme.complitations.UpdateCompilationRequest;

public interface CompilationsRepository {
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId);
}
