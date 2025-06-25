package ru.practicum.explorewithme.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.explorewithme.ViewStats;
import ru.practicum.explorewithme.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@EntityScan(basePackages = "ru.practicum.explorewithme.model")
class StatRepositoryTest {

    @Autowired
    private StatRepository statRepository;

    @Test
    @DisplayName("getTotalStats() - должен возвращать общее количество хитов по URI")
    void getTotalStats_shouldReturnCorrectStats() {
        LocalDateTime now = LocalDateTime.now();

        EndpointHit hit1 = EndpointHit.builder()
                .app("main-service")
                .uri("/events")
                .ip("192.168.0.1")
                .timestamp(now.minusHours(1))
                .build();

        EndpointHit hit2 = EndpointHit.builder()
                .app("main-service")
                .uri("/events")
                .ip("192.168.0.2")
                .timestamp(now.minusMinutes(30))
                .build();

        statRepository.saveAll(List.of(hit1, hit2));

        List<ViewStats> stats = statRepository.getTotalStats(
                now.minusHours(2),
                now,
                List.of("/events")
        );

        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().getHits()).isEqualTo(2);
        assertThat(stats.getFirst().getUri()).isEqualTo("/events");
    }

    @Test
    @DisplayName("getUniqueStats() - должен возвращать количество уникальных IP по URI")
    void getUniqueStats_shouldReturnCorrectUniqueStats() {
        LocalDateTime now = LocalDateTime.now();

        EndpointHit hit1 = EndpointHit.builder()
                .app("main-service")
                .uri("/events")
                .ip("192.168.0.1")
                .timestamp(now.minusHours(1))
                .build();

        EndpointHit hit2 = EndpointHit.builder()
                .app("main-service")
                .uri("/events")
                .ip("192.168.0.1") // тот же IP
                .timestamp(now.minusMinutes(30))
                .build();

        EndpointHit hit3 = EndpointHit.builder()
                .app("main-service")
                .uri("/events")
                .ip("192.168.0.2") // другой IP
                .timestamp(now.minusMinutes(10))
                .build();

        statRepository.saveAll(List.of(hit1, hit2, hit3));

        List<ViewStats> stats = statRepository.getUniqueStats(
                now.minusHours(2),
                now,
                List.of("/events")
        );

        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().getHits()).isEqualTo(2); // 2 уникальных IP
    }

    @Test
    @DisplayName("getTotalStats() - с null uris должен возвращать всё")
    void getTotalStats_nullUris_shouldReturnAllStats() {
        LocalDateTime now = LocalDateTime.now();

        EndpointHit hit = EndpointHit.builder()
                .app("another-service")
                .uri("/other")
                .ip("127.0.0.1")
                .timestamp(now)
                .build();

        statRepository.save(hit);

        List<ViewStats> stats = statRepository.getTotalStats(now.minusHours(1), now.plusHours(1), null);

        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().getUri()).isEqualTo("/other");
    }
}
