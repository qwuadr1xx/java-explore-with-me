package ru.practicum.explorewithme.internal.requests.repository;

import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.util.List;

public interface RequestsRepository {
    List<ParticipationRequestDto> getUsersRequest(Long userId);

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}
