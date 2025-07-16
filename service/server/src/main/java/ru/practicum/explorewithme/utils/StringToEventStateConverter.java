package ru.practicum.explorewithme.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.events.utils.EventState;
import ru.practicum.explorewithme.exception.InvalidStateException;

@Component
public class StringToEventStateConverter implements Converter<String, EventState> {
    @Override
    public EventState convert(String source) {
        try {
            return EventState.valueOf(source);
        } catch (IllegalArgumentException e) {
            throw new InvalidStateException(String.format("Failed to convert value of type java.lang.String to required " +
                    "type EventState; nested exception is InvalidStateException: For input string: %s", source));
        }
    }
}