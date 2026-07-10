package ru.practicum.comment.service;



import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.InputCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;

import java.util.List;
/**
 * Сервисный слой для управления комментариями.
 * <p>Инкапсулирует бизнес-логику приватного, административного и публичного API.</p>
 *
 * <ul>
 *   <li><b>Приватные операции (пользователь → свои комментарии):</b>
 *       {@link #privateAdd(Long, Long, InputCommentDto)},
 *       {@link #privateUpdate(Long, Long, UpdateCommentDto)},
 *       {@link #privateDelete(Long, Long)},
 *       {@link #findCommentsByEventIdAndUserId(Long, Long, Integer, Integer)},
 *       {@link #findCommentsByUserId(Long, Integer, Integer)};</li>
 *   <li><b>Административные операции:</b>
 *       {@link #adminUpdate(Long, UpdateCommentDto)},
 *       {@link #adminDelete(Long)};</li>
 *   <li><b>Публичные операции (доступны всем):</b>
 *       {@link #findCommentsByEventId(Long, Integer, Integer)},
 *       {@link #findCommentById(Long)}.</li>
 * </ul>
 *
 * <p>Все операции возвращают/принимают DTO,
 * {@link ru.practicum.comment.dto.CommentDto}, {@link InputCommentDto}, {@link UpdateCommentDto}.</p>
 */
public interface CommentService {

    CommentDto privateAdd(Long userId, Long eventId, InputCommentDto inputCommentDto);

    CommentDto privateUpdate(Long userId, Long commentId, UpdateCommentDto updateCommentDto);

    CommentDto adminUpdate(Long id, UpdateCommentDto updateCommentDto);

    void adminDelete(Long id);

    void privateDelete(Long userId, Long commentId);

    List<CommentDto> findCommentsByEventId(Long eventId, Integer from, Integer size);

    List<CommentDto> findCommentsByEventIdAndUserId(Long eventId, Long userId, Integer from, Integer size);

    List<CommentDto> findCommentsByUserId(Long userId, Integer from, Integer size);

    CommentDto findCommentById(Long commentId);
}