package ru.practicum.explorewithme.admin.compilations.service;

import ru.practicum.explorewithme.compilations.CompilationDto;
import ru.practicum.explorewithme.compilations.NewCompilationDto;
import ru.practicum.explorewithme.compilations.UpdateCompilationRequest;

public interface CompilationsService {
    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId);
}
