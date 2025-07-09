package ru.practicum.explorewithme.publicapi.events.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.utils.Sort;
import ru.practicum.explorewithme.publicapi.events.repository.EventsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicEventsServiceImpl implements EventsService {
    private final EventsRepository eventsRepository;

    @Override
    public List<EventFullDto> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Boolean onlyAvailable,
                                        Sort sort, Integer from, Integer size, String ipAddress) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        return eventsRepository.getEvents(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, ipAddress);
    }

    @Override
    public EventFullDto getEventById(Long eventId, String ipAddress) {
        return eventsRepository.getEventById(eventId, ipAddress);
    }
}
