package ru.practicum.explorewithme.admin.compilations.service;

import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.complitations.NewCompilationDto;
import ru.practicum.explorewithme.complitations.UpdateCompilationRequest;

public interface CompilationsService {
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId);
}
