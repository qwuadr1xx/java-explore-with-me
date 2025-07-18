package ru.practicum.explorewithme.internal.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.comments.CommentDto;
import ru.practicum.explorewithme.comments.NewCommentDto;
import ru.practicum.explorewithme.comments.util.CommentStatus;
import ru.practicum.explorewithme.internal.comments.repository.CommentsRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivateCommentsServiceImpl implements CommentsService {
    private final CommentsRepository commentsRepository;

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        return commentsRepository.addComment(userId, eventId, newCommentDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long userId, Long eventId, CommentStatus status, Integer from, Integer size) {
        if (eventId != null) {
            return commentsRepository.getUserCommentsOnEvent(userId, eventId, status, from, size);
        } else {
            return commentsRepository.getUserComments(userId, status, from, size);
        }
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto) {
        return commentsRepository.updateComment(userId, commentId, newCommentDto);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long comId) {
        commentsRepository.deleteComment(userId, comId);
    }
}
