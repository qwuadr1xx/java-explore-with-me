package ru.practicum.explorewithme.internal.comments.service;

import ru.practicum.explorewithme.comments.CommentDto;
import ru.practicum.explorewithme.comments.NewCommentDto;
import ru.practicum.explorewithme.comments.util.CommentStatus;

import java.util.List;

public interface CommentsService {
    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    List<CommentDto> getUserComments(Long userId, Long eventId, CommentStatus status, Integer from, Integer size);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto);

    void deleteComment(Long userId, Long commentId);
}
