package ru.practicum.explorewithme.admin.events.repository;

import org.jooq.Record2;
import ru.practicum.explorewithme.comments.CommentDto;
import ru.practicum.explorewithme.comments.util.CommentStatus;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.UpdateEventAdminRequest;
import ru.practicum.explorewithme.events.utils.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventsRepository {
    List<EventFullDto> getEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                 LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEvent(UpdateEventAdminRequest request, Long eventId);

    Record2<LocalDateTime, String> getEventsFieldsForValidation(Long eventId);

    List<CommentDto> getComments(List<Long> events, List<Long> users, CommentStatus status, Integer from, Integer size);

    CommentDto updateComment(Long comId, CommentStatus status);
}
