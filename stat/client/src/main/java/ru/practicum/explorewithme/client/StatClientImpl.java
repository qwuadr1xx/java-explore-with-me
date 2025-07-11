package ru.practicum.explorewithme.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.explorewithme.HitDtoIn;
import org.springframework.core.ParameterizedTypeReference;
import ru.practicum.explorewithme.ViewStats;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatClientImpl implements StatClient {
    private final RestClient restClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatClientImpl(@Value("${ewm-stat-server.url}") String serverUrl) {
        this.restClient = RestClient.builder().baseUrl(serverUrl).build();
    }

    @Override
    public ResponseEntity<Void> saveHit(HitDtoIn hitDtoIn) {
        return restClient.post()
                .uri("/hit")
                .body(hitDtoIn)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public ResponseEntity<List<ViewStats>> getStats(LocalDateTime start,
                                                    LocalDateTime end,
                                                    List<String> uris,
                                                    Boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", FORMATTER.format(start))
                .queryParam("end", FORMATTER.format(end));

        if (unique != null) {
            builder.queryParam("unique", unique);
        }

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris.toArray());
        }

        URI uri = builder.build(true).toUri();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<>() {});
    }
}