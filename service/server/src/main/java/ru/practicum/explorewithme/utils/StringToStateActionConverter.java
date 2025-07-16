package ru.practicum.explorewithme.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.events.utils.StateAction;
import ru.practicum.explorewithme.exception.InvalidStateException;

@Component
public class StringToStateActionConverter implements Converter<String, StateAction> {
    @Override
    public StateAction convert(String source) {
        try {
            return StateAction.valueOf(source);
        } catch (IllegalArgumentException e) {
            throw new InvalidStateException(String.format("Failed to convert value of type java.lang.String to required " +
                    "type StateAction; nested exception is InvalidStateException: For input string: %s", source));
        }
    }
}
