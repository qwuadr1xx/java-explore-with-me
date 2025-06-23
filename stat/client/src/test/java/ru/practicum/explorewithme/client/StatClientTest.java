package ru.practicum.explorewithme.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.explorewithme.HitDtoIn;
import ru.practicum.explorewithme.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class StatClientTest {

    private StatClientImpl spyClient;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:9090";
        StatClientImpl realClient = new StatClientImpl(baseUrl);
        spyClient = spy(realClient);
    }

    @Test
    void saveHit_ShouldUsePostAndReturnVoid() {
        HitDtoIn hitDto = HitDtoIn.builder()
                .app("app1")
                .uri("/test")
                .ip("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();

        doReturn(ResponseEntity.status(HttpStatus.CREATED).build())
                .when(spyClient).saveHit(hitDto);

        ResponseEntity<Void> response = spyClient.saveHit(hitDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getStats_ShouldUseGetAndReturnList() {
        LocalDateTime start = LocalDateTime.of(2025, 6, 22, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 6, 23, 0, 0);
        List<String> uris = List.of("/a", "/b");
        Boolean unique = true;

        ViewStats stat = ViewStats.builder()
                .app("app1")
                .uri("/a")
                .hits(5L)
                .build();
        List<ViewStats> expectedList = List.of(stat);

        doReturn(ResponseEntity.ok(expectedList))
                .when(spyClient).getStats(start, end, uris, unique);

        ResponseEntity<List<ViewStats>> response =
                spyClient.getStats(start, end, uris, unique);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(5L, response.getBody().getFirst().getHits());
    }
}