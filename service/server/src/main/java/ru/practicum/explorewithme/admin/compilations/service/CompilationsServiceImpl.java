package ru.practicum.explorewithme.admin.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.admin.compilations.repository.CompilationsRepository;
import ru.practicum.explorewithme.complitations.CompilationDto;
import ru.practicum.explorewithme.complitations.NewCompilationDto;

@Service
@RequiredArgsConstructor
public class CompilationsServiceImpl implements CompilationsService {
    CompilationsRepository compilationsRepository;

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        return compilationsRepository.createCompilation(newCompilationDto);
    }

    @Override
    public void deleteCompilation(Long compId) {
        compilationsRepository.deleteCompilation(compId);
    }

    @Override
    public CompilationDto updateCompilation(NewCompilationDto newCompilationDto, Long compId) {
        return compilationsRepository.updateCompilation(newCompilationDto, compId);
    }
}
