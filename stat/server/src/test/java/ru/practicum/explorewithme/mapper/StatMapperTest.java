package ru.practicum.explorewithme.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.explorewithme.HitDtoIn;
import ru.practicum.explorewithme.model.EndpointHit;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class StatMapperTest {

    @Test
    void toEndpointHit_shouldMapCorrectly() {
        HitDtoIn dto = new HitDtoIn("app", "/uri", "192.168.0.1", LocalDateTime.now());

        EndpointHit hit = StatMapper.toEndpointHit(dto);

        assertThat(hit.getApp()).isEqualTo(dto.getApp());
        assertThat(hit.getUri()).isEqualTo(dto.getUri());
        assertThat(hit.getIp()).isEqualTo(dto.getIp());
        assertThat(hit.getTimestamp()).isEqualTo(dto.getTimestamp());
    }
}

