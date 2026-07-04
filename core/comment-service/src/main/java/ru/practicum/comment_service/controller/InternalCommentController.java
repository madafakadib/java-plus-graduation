package ru.practicum.comment_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment_service.service.InternalCommentService;
import ru.practicum.common.commentDto.dto.CommentCountDto;
import ru.practicum.common.commentDto.enums.CommentStatus;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/internal/comments")
@RequiredArgsConstructor
public class InternalCommentController {

    private final InternalCommentService internalCommentService;

    @GetMapping("/count-by-event-ids")
    public List<CommentCountDto> countByEventIdInAndStatus(
            @RequestParam("eventIds") List<Long> eventIds,
            @RequestParam("status") CommentStatus status) {
        log.info("Internal API: Counting comments for eventIds: {}, status: {}", eventIds, status);
        return internalCommentService.countByEventIdInAndStatus(eventIds, status);
    }
}