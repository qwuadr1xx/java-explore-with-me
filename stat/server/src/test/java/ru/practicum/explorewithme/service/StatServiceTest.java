package ru.practicum.explorewithme.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.HitDtoIn;
import ru.practicum.explorewithme.ViewStats;
import ru.practicum.explorewithme.exception.InvalidDateException;
import ru.practicum.explorewithme.model.EndpointHit;
import ru.practicum.explorewithme.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatServiceTest {

    @Mock
    StatRepository statRepository;

    @InjectMocks
    StatServiceImpl statService;

    @Test
    void addHit_shouldCallRepositorySave() {
        HitDtoIn hit = new HitDtoIn("app", "/uri", "127.0.0.1", LocalDateTime.now());

        statService.addHit(hit);

        verify(statRepository).save(any(EndpointHit.class));
    }

    @Test
    void getStats_shouldThrowInvalidDateException() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 23, 12, 0);
        LocalDateTime end = LocalDateTime.of(2025, 6, 23, 10, 0);

        assertThrows(InvalidDateException.class, () -> statService.getStats(start, end, null, false));
    }

    @Test
    void getStats_shouldCallTotalStats() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 23, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 6, 23, 12, 0);
        when(statRepository.getTotalStats(start, end, null)).thenReturn(List.of());

        List<ViewStats> result = statService.getStats(start, end, null, false);

        assertThat(result).isEmpty();
        verify(statRepository).getTotalStats(start, end, null);
    }

    @Test
    void getStats_shouldCallUniqueStats() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 23, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 6, 23, 12, 0);
        when(statRepository.getUniqueStats(start, end, null)).thenReturn(List.of());

        List<ViewStats> result = statService.getStats(start, end, null, true);

        assertThat(result).isEmpty();
        verify(statRepository).getUniqueStats(start, end, null);
    }
}