package ru.practicum.explorewithme.publicapi.compilations.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.compilations.CompilationDto;
import ru.practicum.explorewithme.publicapi.compilations.repository.CompilationsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicCompilationsServiceImpl implements CompilationsService {
    private final CompilationsRepository compilationsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        return compilationsRepository.getCompilations(pinned, from, size);
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        return compilationsRepository.getCompilationById(compId);
    }
}
