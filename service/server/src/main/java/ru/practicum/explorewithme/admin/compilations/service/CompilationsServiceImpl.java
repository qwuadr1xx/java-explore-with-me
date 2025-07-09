package ru.practicum.explorewithme.admin.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.admin.compilations.repository.CompilationsRepository;
import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.complitations.NewCompilationDto;
import ru.practicum.explorewithme.complitations.UpdateCompilationRequest;

@Service
@RequiredArgsConstructor
public class CompilationsServiceImpl implements CompilationsService {
    private final CompilationsRepository compilationsRepository;

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        return compilationsRepository.createCompilation(newCompilationDto);
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationsRepository.deleteCompilation(compId);
    }

    @Override
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId) {
        return compilationsRepository.updateCompilation(updateCompilationRequest, compId);
    }
}
