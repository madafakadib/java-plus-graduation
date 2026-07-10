package ru.practicum.comment.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.InputCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.service.CommentService;

import java.util.List;
/**
 * Приватный REST-контроллер для работы с комментариями пользователями.
 * <p>Позволяет авторизованным пользователям создавать, изменять, удалять
 * и просматривать собственные комментарии к событиям.</p>
 *
 * <ul>
 *   <li>POST /comments/{eventId}/{userId} — добавить комментарий к событию;</li>
 *   <li>DELETE /comments/{commentId}/{userId} — удалить свой комментарий;</li>
 *   <li>PATCH /comments/{commentId}/{userId} — обновить свой комментарий;</li>
 *   <li>GET /comments/{eventId}/{userId} — список комментариев пользователя для события;</li>
 *   <li>GET /comments/users/{userId} — все комментарии пользователя.</li>
 * </ul>
 *
 * <p>Валидация запроса выполняется через {@link jakarta.validation.Valid}.
 * Логика реализована в {@link ru.practicum.comment.service.CommentService}.</p>
 */
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{eventId}/{userId}")
    public CommentDto addComment(@PathVariable(name = "eventId") Long eventId,
                                 @PathVariable(name = "userId") Long userId,
                                 @Valid @RequestBody InputCommentDto inputCommentDto) {
        return commentService.privateAdd(userId, eventId, inputCommentDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{commentId}/{userId}")
    public void deleteComment(@PathVariable(name = "commentId") Long commentId,
                              @PathVariable(name = "userId") Long userId) {
        commentService.privateDelete(userId, commentId);
    }

    @PatchMapping("/{commentId}/{userId}")
    public CommentDto updateComment(@PathVariable(name = "commentId") Long commentId,
                                    @PathVariable(name = "userId") Long userId,
                                    @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        return commentService.privateUpdate(userId, commentId, updateCommentDto);
    }

    @GetMapping("/{eventId}/{userId}")
    public List<CommentDto> findCommentsByEventIdAndUserId(@PathVariable(name = "eventId") Long eventId,
                                                           @PathVariable(name = "userId") Long userId,
                                                           @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                           @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return commentService.findCommentsByEventIdAndUserId(eventId, userId, from, size);
    }

    @GetMapping("/users/{userId}")
    public List<CommentDto> findCommentsByUserId(@PathVariable(name = "userId") Long userId,
                                                 @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                 @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return commentService.findCommentsByUserId(userId, from, size);
    }
}