package ru.practicum.explorewithme.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.events.utils.Sort;
import ru.practicum.explorewithme.exception.InvalidStateException;

@Component
public class StringToSortConverter implements Converter<String, Sort> {
    @Override
    public Sort convert(String source) {
        try {
            return Sort.valueOf(source);
        } catch (IllegalArgumentException e) {
            throw new InvalidStateException(String.format("Failed to convert value of type java.lang.String to required " +
                    "type Sort; nested exception is InvalidStateException: For input string: %s", source));
        }
    }
}