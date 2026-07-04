package ru.practicum.comment_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.common.commentDto.dto.CommentCountDto;
import ru.practicum.common.commentDto.enums.CommentStatus;
import ru.practicum.comment_service.repository.CommentRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalCommentService {

    private final CommentRepository commentRepository;

    public List<CommentCountDto> countByEventIdInAndStatus(List<Long> eventIds, CommentStatus status) {
        return commentRepository.countByEventIdInAndStatus(eventIds, status);
    }
}