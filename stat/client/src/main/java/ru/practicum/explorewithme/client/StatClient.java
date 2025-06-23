package ru.practicum.explorewithme.client;

import org.springframework.http.ResponseEntity;
import ru.practicum.explorewithme.HitDtoIn;
import ru.practicum.explorewithme.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatClient {
    ResponseEntity<Void> saveHit(HitDtoIn hitDtoIn);

    ResponseEntity<List<ViewStats>> getStats(LocalDateTime start,
                                             LocalDateTime end,
                                             List<String> uris,
                                             Boolean unique);
}
