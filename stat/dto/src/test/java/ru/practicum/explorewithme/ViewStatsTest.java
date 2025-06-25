package ru.practicum.explorewithme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

public class ViewStatsTest {

    private JacksonTester<ViewStats> json;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JacksonTester.initFields(this, mapper);
    }

    @Test
    void serializeViewStats() throws Exception {
        ViewStats stats = ViewStats.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .hits(10L)
                .build();

        JsonContent<ViewStats> result = json.write(stats);

        assertThat(result).extractingJsonPathStringValue("$.app").isEqualTo("ewm-main-service");
        assertThat(result).extractingJsonPathStringValue("$.uri").isEqualTo("/events/1");
        assertThat(result).extractingJsonPathNumberValue("$.hits").isEqualTo(10);
    }

    @Test
    void deserializeViewStats() throws Exception {
        String input = "{"
                + "\"app\":\"ewm-main-service\","
                + "\"uri\":\"/events/3\","
                + "\"hits\":5"
                + "}";

        ViewStats stats = json.parseObject(input);

        assertThat(stats.getApp()).isEqualTo("ewm-main-service");
        assertThat(stats.getUri()).isEqualTo("/events/3");
        assertThat(stats.getHits()).isEqualTo(5L);
    }
}