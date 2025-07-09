package ru.practicum.explorewithme.internal.requests.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.internal.requests.repository.RequestsRepository;
import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateRequestsServiceImpl implements RequestsService {
    private final RequestsRepository requestsRepository;


    @Override
    public List<ParticipationRequestDto> getUsersRequest(Long userId) {
        return requestsRepository.getUsersRequest(userId);
    }

    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        return requestsRepository.addRequest(userId, eventId);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        return requestsRepository.cancelRequest(userId, requestId);
    }
}
