package ru.practicum.explorewithme.utils;

import org.jooq.Record;
import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.Location;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.jooq.tables.Categories;
import ru.practicum.explorewithme.jooq.tables.Events;
import ru.practicum.explorewithme.jooq.tables.Locations;
import ru.practicum.explorewithme.jooq.tables.Users;
import ru.practicum.explorewithme.users.UserShortDto;

public class RecordToEventMapper {
    public static EventFullDto map(Record record) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(record.get(Events.EVENTS.ID));
        eventFullDto.setAnnotation(record.get(Events.EVENTS.ANNOTATION));
        eventFullDto.setConfirmedRequests(record.get(Events.EVENTS.CONFIRMED_REQUESTS));
        eventFullDto.setCreatedOn(record.get(Events.EVENTS.CREATED_ON));
        eventFullDto.setDescription(record.get(Events.EVENTS.DESCRIPTION));
        eventFullDto.setEventDate(record.get(Events.EVENTS.EVENT_DATE));
        eventFullDto.setPaid(record.get(Events.EVENTS.PAID));
        eventFullDto.setParticipantLimit(record.get(Events.EVENTS.PARTICIPANT_LIMIT));
        eventFullDto.setPublishedOn(record.get(Events.EVENTS.PUBLISHED_ON));
        eventFullDto.setRequestModeration(record.get(Events.EVENTS.REQUEST_MODERATION));
        eventFullDto.setState(EventState.valueOf(record.get(Events.EVENTS.STATE)));
        eventFullDto.setTitle(record.get(Events.EVENTS.TITLE));
        eventFullDto.setViews(record.get(Events.EVENTS.VIEWS));

        eventFullDto.setLocation(
                new Location(
                        record.get(Locations.LOCATIONS.LAT),
                        record.get(Locations.LOCATIONS.LON)
                )
        );

        eventFullDto.setCategory(
                new CategoryDto(
                        record.get(Categories.CATEGORIES.ID),
                        record.get(Categories.CATEGORIES.NAME)
                )
        );

        eventFullDto.setInitiator(
                new UserShortDto(
                        record.get(Users.USERS.ID),
                        record.get(Users.USERS.NAME)
                )
        );

        return eventFullDto;
    }
}
