package ru.practicum.explorewithme.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.events.utils.EventStatus;
import ru.practicum.explorewithme.exception.InvalidStateException;

@Component
public class StringToEventStatusConverter implements Converter<String, EventStatus> {
    @Override
    public EventStatus convert(String source) {
        try {
            return EventStatus.valueOf(source);
        } catch (IllegalArgumentException e) {
            throw new InvalidStateException(String.format("Failed to convert value of type java.lang.String to required " +
                    "type EventStatus; nested exception is InvalidStateException: For input string: %s", source));
        }
    }
}
