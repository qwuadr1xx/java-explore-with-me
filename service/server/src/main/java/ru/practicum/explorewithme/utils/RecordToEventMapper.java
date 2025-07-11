package ru.practicum.explorewithme.utils;

import org.jooq.Record;
import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.events.EventFullDto;
import ru.practicum.explorewithme.events.Location;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Categories;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Locations;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Users;
import ru.practicum.explorewithme.users.UserShortDto;

public class RecordToEventMapper {
    public static EventFullDto map(Record record) {
        EventFullDto eventFullDto = record.into(EventFullDto.class);

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
