package ru.practicum.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.common.commentDto.dto.CommentCountDto;
import ru.practicum.common.commentDto.enums.CommentStatus;

import java.util.List;

@FeignClient(name = "comment-service", path = "/api/internal/comments")
public interface CommentClient {

    @GetMapping("/count-by-event-ids")
    List<CommentCountDto> countByEventIdInAndStatus(
            @RequestParam("eventIds") List<Long> eventIds,
            @RequestParam("status") CommentStatus status);
}