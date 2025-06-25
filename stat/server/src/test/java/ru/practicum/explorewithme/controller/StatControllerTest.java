package ru.practicum.explorewithme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.HitDtoIn;
import ru.practicum.explorewithme.ViewStats;
import ru.practicum.explorewithme.exception.ErrorHandler;
import ru.practicum.explorewithme.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatController.class)
@Import(ErrorHandler.class)
class StatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatService statService;

    @Autowired
    private ObjectMapper mapper;

    private static final String BASE_STATS = "/stats";
    private static final String BASE_HIT = "/hit";

    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void init() {
        mapper.registerModule(new JavaTimeModule());
        start = LocalDateTime.of(2025,6,23,10,0,0);
        end = LocalDateTime.of(2025,6,23,12,0,0);
    }

    @Test
    void addHit_ShouldReturnOk() throws Exception {
        HitDtoIn dto = HitDtoIn.builder()
                .app("app1")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(start)
                .build();
        mockMvc.perform(post(BASE_HIT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void getViewStats_ShouldReturnList() throws Exception {
        ViewStats stats = new ViewStats("app1","/test",5L);
        when(statService.getStats(eq(start), eq(end), isNull(), eq(Boolean.FALSE)))
                .thenReturn(List.of(stats));

        mockMvc.perform(get(BASE_STATS)
                        .param("start","2025-06-23 10:00:00")
                        .param("end","2025-06-23 12:00:00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].app").value("app1"))
                .andExpect(jsonPath("$[0].uri").value("/test"))
                .andExpect(jsonPath("$[0].hits").value(5));
    }

    @Test
    void getViewStats_WithUniqueTrue_ShouldPassUnique() throws Exception {
        ViewStats stats = new ViewStats("app1","/u",1L);
        when(statService.getStats(eq(start), eq(end), anyList(), eq(Boolean.TRUE)))
                .thenReturn(List.of(stats));

        mockMvc.perform(get(BASE_STATS)
                        .param("start","2025-06-23 10:00:00")
                        .param("end","2025-06-23 12:00:00")
                        .param("unique","true")
                        .param("uris","/u","/v"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hits").value(1));
    }
}