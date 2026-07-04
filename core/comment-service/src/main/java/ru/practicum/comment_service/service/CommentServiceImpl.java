package ru.practicum.comment_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment_service.mapper.CommentMapper;
import ru.practicum.comment_service.model.Comment;
import ru.practicum.comment_service.repository.CommentRepository;
import ru.practicum.common.client.EventClient;
import ru.practicum.common.client.UserClient;
import ru.practicum.common.commentDto.dto.*;
import ru.practicum.common.commentDto.enums.CommentStatus;
import ru.practicum.common.eventDto.dto.enums.EventState;
import ru.practicum.common.exceptions.exceptions.ConditionsNotMetException;
import ru.practicum.common.exceptions.exceptions.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Override
    public List<CommentShortDto> getEventComments(Long eventId, int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByEventIdAndStatusOrderByCreatedDesc(
                eventId, CommentStatus.APPROVED, page);  // публичный запрос, по этому только APPROVED
        return comments.stream()
                .map(CommentMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentShortDto getComment(Long commentId) {
        Comment comment = commentRepository.findByIdAndStatus(commentId, CommentStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found or not approved"));
        return CommentMapper.toShortDto(comment);
    }

    @Override
    @Transactional
    public CommentFullDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        if (!userClient.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " not found");
        }

        if (!eventClient.existsById(eventId)) {
            throw new NotFoundException("Event with id=" + eventId + " not found");
        }

        var event = eventClient.getEventById(eventId);
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Cannot comment on unpublished event");
        }

        Comment comment = Comment.builder()
                .text(dto.getText())
                .authorId(userId)
                .eventId(eventId)
                .status(CommentStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        comment = commentRepository.save(comment);

        return CommentMapper.toFullDto(comment);
    }

    @Override
    @Transactional
    public CommentFullDto updateCommentByUser(Long userId, Long commentId, UpdateCommentUserRequest dto) {
        // пользователь может менять статус только на DELETE
        if (dto.getStatus() != null && dto.getStatus() != CommentStatus.DELETED) {
            throw new ConditionsNotMetException("User can only set status to DELETED");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));

        if (!comment.getAuthorId().equals(userId)) {
            throw new ConditionsNotMetException("Only author can update their comments");
        }

        // если меняем текст у коммента в статусе кроме PENDING, тогда ошибкО
        if (dto.getText() != null && comment.getStatus() != CommentStatus.PENDING) {
            throw new ConditionsNotMetException("Text can only be changed when comment is in PENDING status");
        }

        CommentMapper.updateCommentFromUserRequest(dto, comment);
        comment = commentRepository.save(comment);
        return CommentMapper.toFullDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));

        if (!comment.getAuthorId().equals(userId)) {
            throw new ConditionsNotMetException("Only author can delete their comments");
        }

        comment.setStatus(CommentStatus.DELETED);
        comment.setUpdated(LocalDateTime.now());
        commentRepository.save(comment);
    }

    @Override
    public List<CommentFullDto> getCommentsForModeration(int from, int size) {
        PageRequest page = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByStatusOrderByCreatedAsc(CommentStatus.PENDING, page);
        return comments.stream()
                .map(CommentMapper::toFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentFullDto moderateComment(Long moderatorId, Long commentId, UpdateCommentAdminRequest dto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));

        if (!userClient.existsById(moderatorId)) {
            throw new NotFoundException("Moderator with id=" + moderatorId + " not found");
        }
        CommentMapper.updateCommentFromAdminRequest(dto, comment, moderatorId);
        comment = commentRepository.save(comment);
        return CommentMapper.toFullDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long moderatorId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));

        if (!userClient.existsById(moderatorId)) {
            throw new NotFoundException("Moderator with id=" + moderatorId + " not found");
        }
        CommentMapper.adminDeleteComment(comment, moderatorId);
        commentRepository.save(comment);
    }
}