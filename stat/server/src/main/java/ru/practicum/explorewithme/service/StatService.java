package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.HitDtoIn;
import ru.practicum.explorewithme.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {
    List<ViewStats> getStats(LocalDateTime start,
                             LocalDateTime end,
                             List<String> uris,
                             Boolean unique);

    void addHit(HitDtoIn hitDtoIn);
}
