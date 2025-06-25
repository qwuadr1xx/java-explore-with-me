package ru.practicum.explorewithme.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EndpointHitTest {

    @Test
    void builderShouldCreateValidEntity() {
        LocalDateTime timestamp = LocalDateTime.now();

        EndpointHit hit = EndpointHit.builder()
                .id(1L)
                .app("service")
                .uri("/endpoint")
                .ip("10.0.0.1")
                .timestamp(timestamp)
                .build();

        assertThat(hit.getId()).isEqualTo(1L);
        assertThat(hit.getApp()).isEqualTo("service");
        assertThat(hit.getUri()).isEqualTo("/endpoint");
        assertThat(hit.getIp()).isEqualTo("10.0.0.1");
        assertThat(hit.getTimestamp()).isEqualTo(timestamp);
    }
}
