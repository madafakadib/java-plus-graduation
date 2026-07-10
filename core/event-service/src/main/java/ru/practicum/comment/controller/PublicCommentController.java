package ru.practicum.comment.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.Collection;
/**
 * Публичный REST-контроллер для работы с комментариями.
 * <p>Доступен без авторизации. Предоставляет только операции чтения.</p>
 *
 * <ul>
 *   <li>GET /comments/events/{eventId} — постраничное получение комментариев к событию;</li>
 *   <li>GET /comments/{commentId} — получение комментария по идентификатору.</li>
 * </ul>
 *
 * <p>Бизнес-логика реализована в {@link ru.practicum.comment.service.CommentService}.</p>
 */
@RestController
@RequestMapping(path = "/comments")
@RequiredArgsConstructor
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping("/events/{eventId}")
    public Collection<CommentDto> findCommentsByEventId(@PathVariable(name = "eventId") Long eventId,
                                                        @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                        @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return commentService.findCommentsByEventId(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto findCommentById(@PathVariable(name = "commentId") Long commentId) {
        return commentService.findCommentById(commentId);
    }
}