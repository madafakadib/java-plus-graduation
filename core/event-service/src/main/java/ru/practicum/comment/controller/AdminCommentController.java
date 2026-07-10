package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.*;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.service.CommentService;

/**
 * Админский REST-контроллер для управления комментариями.
 * <p>Предоставляет операции модерации, доступные только администраторам.</p>
 *
 * <ul>
 *   <li>DELETE /admin/comments/{commentId} — удаление комментария;</li>
 *   <li>PATCH /admin/comments/{commentId} — обновление текста или состояния комментария.</li>
 * </ul>
 *
 * <p>Все запросы проходят валидацию входных данных.
 * Используется {@link CommentService} как слой бизнес-логики.</p>
 */
@RestController
@RequestMapping(path = "/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("commentId") long id) {
        commentService.adminDelete(id);
    }

    @PatchMapping("/{commentId}")
    public CommentDto update(@PathVariable("commentId") long id,
                             @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        return commentService.adminUpdate(id, updateCommentDto);
    }

}