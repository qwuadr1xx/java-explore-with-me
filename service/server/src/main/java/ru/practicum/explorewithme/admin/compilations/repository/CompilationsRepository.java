package ru.practicum.explorewithme.admin.compilations.repository;

import ru.practicum.explorewithme.compilations.CompilationDto;
import ru.practicum.explorewithme.compilations.NewCompilationDto;
import ru.practicum.explorewithme.compilations.UpdateCompilationRequest;

public interface CompilationsRepository {
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId);
}
