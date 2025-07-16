package ru.practicum.explorewithme.admin.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.admin.compilations.repository.CompilationsRepository;
import ru.practicum.explorewithme.compilations.CompilationDto;
import ru.practicum.explorewithme.compilations.NewCompilationDto;
import ru.practicum.explorewithme.compilations.UpdateCompilationRequest;

@Service
@RequiredArgsConstructor
public class CompilationsServiceImpl implements CompilationsService {
    private final CompilationsRepository compilationsRepository;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        return compilationsRepository.createCompilation(newCompilationDto);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        compilationsRepository.deleteCompilation(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId) {
        return compilationsRepository.updateCompilation(updateCompilationRequest, compId);
    }
}
