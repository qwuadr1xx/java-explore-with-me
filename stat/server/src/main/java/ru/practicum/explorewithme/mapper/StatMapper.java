package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.HitDtoIn;
import ru.practicum.explorewithme.model.EndpointHit;

public class StatMapper {
    public static EndpointHit toEndpointHit(HitDtoIn hitDtoIn) {
        return EndpointHit.builder()
                .app(hitDtoIn.getApp())
                .ip(hitDtoIn.getIp())
                .timestamp(hitDtoIn.getTimestamp())
                .uri(hitDtoIn.getUri())
                .build();
    }
}
