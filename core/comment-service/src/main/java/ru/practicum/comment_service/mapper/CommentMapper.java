package ru.practicum.comment_service.mapper;

import lombok.RequiredArgsConstructor;
import ru.practicum.comment_service.model.Comment;
import ru.practicum.common.commentDto.dto.*;
import ru.practicum.common.commentDto.enums.CommentStatus;


import java.time.LocalDateTime;


@RequiredArgsConstructor
public class CommentMapper {

    public static Comment toComment(NewCommentDto dto, Long author, Long event) {
        return Comment.builder()
                .text(dto.getText())
                .authorId(author)
                .eventId(event)
                .created(LocalDateTime.now())
                .status(CommentStatus.PENDING)
                .build();
    }

    public static void updateCommentFromUserRequest(UpdateCommentUserRequest request, Comment comment) {
        boolean updated = false;
        if (request.getText() != null) {
            comment.setText(request.getText());
            updated = true;
        }
        if (request.getStatus() != null) {
            comment.setStatus(CommentStatus.DELETED); // в сервисе уже проверили, что статус если пришел в запросе, то только DELETE
            updated = true;
        }
        if (updated) {
            comment.setUpdated(LocalDateTime.now());
        }
    }

    public static void updateCommentFromAdminRequest(UpdateCommentAdminRequest request, Comment comment, Long moderator) {
        boolean updated = false;

        if (request.getText() != null) {
            comment.setText(request.getText());
            updated = true;
        }
        if (request.getStatus() != null) {
            comment.setStatus(request.getStatus());
            comment.setModeratorId(moderator);
            comment.setModeratedAt(LocalDateTime.now());
            updated = true;
        }
        if (updated) {
            comment.setUpdated(LocalDateTime.now());
        }
    }

    public static CommentFullDto toFullDto(Comment comment) {
        CommentFullDto.CommentFullDtoBuilder builder = CommentFullDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthorId())
                .eventId(comment.getEventId())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .status(comment.getStatus().name());

        if (comment.getModeratorId() != null) {
            builder.moderatorId(comment.getModeratorId())
                    .moderatedAt(comment.getModeratedAt());
        }

        return builder.build();
    }

    public static CommentShortDto toShortDto(Comment comment) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthorId())
                .eventId(comment.getEventId())
                .created(comment.getCreated())
                .build();
    }

    public static void adminDeleteComment(Comment comment, Long moderator) {
        comment.setStatus(CommentStatus.DELETED);
        comment.setModeratorId(moderator);
        comment.setModeratedAt(LocalDateTime.now());
        comment.setUpdated(LocalDateTime.now());
    }


}