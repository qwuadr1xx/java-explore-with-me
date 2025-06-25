package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.HitDtoIn;
import ru.practicum.explorewithme.ViewStats;
import ru.practicum.explorewithme.exception.InvalidDateException;
import ru.practicum.explorewithme.mapper.StatMapper;
import ru.practicum.explorewithme.model.EndpointHit;
import ru.practicum.explorewithme.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatRepository statRepository;

    @Override
    public void addHit(HitDtoIn hitDtoIn) {
        EndpointHit hit = StatMapper.toEndpointHit(hitDtoIn);
        statRepository.save(hit);
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new InvalidDateException(start, end);
        }

        return Boolean.TRUE.equals(unique)
                ? statRepository.getUniqueStats(start, end, uris)
                : statRepository.getTotalStats(start, end, uris);
    }
}
