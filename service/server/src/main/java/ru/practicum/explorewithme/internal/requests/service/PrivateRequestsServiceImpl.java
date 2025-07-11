package ru.practicum.explorewithme.internal.requests.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.internal.requests.repository.RequestsRepository;
import ru.practicum.explorewithme.requests.ParticipationRequestDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateRequestsServiceImpl implements RequestsService {
    private final RequestsRepository requestsRepository;


    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUsersRequest(Long userId) {
        return requestsRepository.getUsersRequest(userId);
    }

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        return requestsRepository.addRequest(userId, eventId);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        return requestsRepository.cancelRequest(userId, requestId);
    }
}
