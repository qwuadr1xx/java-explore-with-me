package ru.practicum.explorewithme.utils;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.comments.util.UpdateCommentStatus;
import ru.practicum.explorewithme.exception.InvalidStateException;

@Component
public class StringToUpdateCommentStatusConverter implements Converter<String, UpdateCommentStatus> {
    @Override
    public UpdateCommentStatus convert(String source) {
        try {
            return UpdateCommentStatus.valueOf(source);
        } catch (IllegalArgumentException e) {
            throw new InvalidStateException(String.format("Failed to convert value of type java.lang.String to required " +
                    "type UpdateCommentStatus; nested exception is InvalidStateException: For input string: %s", source));
        }
    }
}

