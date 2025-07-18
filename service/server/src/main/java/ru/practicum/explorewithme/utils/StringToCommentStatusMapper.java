package ru.practicum.explorewithme.utils;

import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;
import ru.practicum.explorewithme.comments.util.CommentStatus;
import ru.practicum.explorewithme.exception.InvalidStateException;

@Component
public class StringToCommentStatusMapper implements Converter<String, CommentStatus> {
    @Override
    public CommentStatus convert(String source) {
        try {
            return CommentStatus.valueOf(source);
        } catch (IllegalArgumentException e) {
            throw new InvalidStateException(String.format("Failed to convert value of type java.lang.String to required " +
                    "type CommentStatus; nested exception is InvalidStateException: For input string: %s", source));
        }
    }
}
