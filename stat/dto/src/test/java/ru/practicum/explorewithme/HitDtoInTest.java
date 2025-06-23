package ru.practicum.explorewithme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class HitDtoInTest {

    private JacksonTester<HitDtoIn> json;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JacksonTester.initFields(this, mapper);
    }

    @Test
    void serializeHitDtoIn() throws Exception {
        HitDtoIn dto = HitDtoIn.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.0.1")
                .timestamp(LocalDateTime.of(2025, 6, 23, 15, 30, 45))
                .build();

        JsonContent<HitDtoIn> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo("ewm-main-service");
        assertThat(result).extractingJsonPathStringValue("$.uri").isEqualTo("/events/1");
        assertThat(result).extractingJsonPathStringValue("$.ip").isEqualTo("192.168.0.1");
        assertThat(result).extractingJsonPathStringValue("$.timestamp").isEqualTo("2025-06-23 15:30:45");
    }

    @Test
    void deserializeHitDtoIn() throws Exception {
        String input = "{"
                + "\"app\":\"ewm-main-service\","
                + "\"uri\":\"/events/2\","
                + "\"ip\":\"127.0.0.1\","
                + "\"timestamp\":\"2025-06-24 08:15:00\""
                + "}";

        HitDtoIn dto = json.parseObject(input);

        assertThat(dto.getApp()).isEqualTo("ewm-main-service");
        assertThat(dto.getUri()).isEqualTo("/events/2");
        assertThat(dto.getIp()).isEqualTo("127.0.0.1");
        assertThat(dto.getTimestamp()).isEqualTo(LocalDateTime.of(2025, 6, 24, 8, 15, 0));
    }
}