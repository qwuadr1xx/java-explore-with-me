package ru.practicum.explorewithme.publicapi.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.HitDtoIn;
import ru.practicum.explorewithme.client.StatClient;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.utils.Sort;
import ru.practicum.explorewithme.publicapi.events.repository.EventsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicEventsServiceImpl implements EventsService {
    private final EventsRepository eventsRepository;
    private final StatClient statClient;

    @Override
    @Transactional
    public List<EventFullDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Boolean onlyAvailable,
                                        Sort sort, Integer from, Integer size, String ipAddress) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        List<EventFullDto> eventFullDtoList =  eventsRepository.getEvents(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);

        if (eventFullDtoList.isEmpty()) {
            return List.of();
        }

        eventFullDtoList.stream().map(EventFullDto::getId).forEach(id -> {
            addView(id);
            updateEventStat(id, ipAddress);
        });

        return eventFullDtoList;
    }

    @Override
    @Transactional
    public EventFullDto getEventById(Long eventId, String ipAddress) {
        EventFullDto eventFullDto = eventsRepository.getEventById(eventId);
        addView(eventId);
        updateEventStat(eventId, ipAddress);
        return eventFullDto;
    }

    private void addView(Long eventId) {
        eventsRepository.addView(eventId);
    }

    private void updateEventStat(Long eventId, String ipAddress) {
        statClient.saveHit(HitDtoIn.builder()
                .app("ewm-main-service")
                .uri("/events/" + eventId)
                .timestamp(LocalDateTime.now())
                .ip(ipAddress)
                .build());
    }
}
