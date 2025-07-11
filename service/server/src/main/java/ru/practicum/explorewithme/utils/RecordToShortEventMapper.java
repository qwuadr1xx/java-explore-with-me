package ru.practicum.explorewithme.utils;

import org.jooq.Record;
import ru.practicum.explorewithme.categories.CategoryDto;
import ru.practicum.explorewithme.events.EventShortDto;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Categories;
import ru.practicum.explorewithme.jooq.ru.explorewithme.jooq.tables.Users;
import ru.practicum.explorewithme.users.UserShortDto;

public class RecordToShortEventMapper {
    public static EventShortDto map(Record record) {
        EventShortDto eventShortDto = new EventShortDto();

        eventShortDto.setCategory(
                new CategoryDto(
                        record.get(Categories.CATEGORIES.ID),
                        record.get(Categories.CATEGORIES.NAME)
                )
        );

        eventShortDto.setInitiator(
                new UserShortDto(
                        record.get(Users.USERS.ID),
                        record.get(Users.USERS.NAME)
                )
        );

        return eventShortDto;
    }
}
