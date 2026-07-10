package ru.practicum.comment.service;


import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.InputCommentDto;
import ru.practicum.comment.dto.UpdateCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.dto.EventState;
import ru.practicum.dto.UserShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.exception.InitiatorRequestException;
import ru.practicum.exception.ValidationException;
import ru.practicum.feign.RequestFeign;
import ru.practicum.feign.UserFeign;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * Реализация {@link CommentService}.
 * <p>Инкапсулирует бизнес-логику управления комментариями пользователей:</p>
 *
 * <ul>
 *   <li>Приватные операции: создание, редактирование и удаление собственных комментариев;</li>
 *   <li>Административные операции: обновление и удаление комментариев без ограничений;</li>
 *   <li>Публичные операции: получение комментариев по событию, пользователю или идентификатору.</li>
 * </ul>
 *
 * <p>Используемые зависимости:</p>
 * <ul>
 *   <li>{@link CommentRepository} — доступ к таблице {@code comments};</li>
 *   <li>{@link EventRepository} — валидация существования и статуса события;</li>
 *   <li>{@link UserFeign} — обращение к user-service для загрузки авторов комментариев;</li>
 *   <li>{@link RequestFeign} — проверка наличия заявки пользователя на событие перед созданием комментария;</li>
 *   <li>{@link CommentMapper} — преобразование между сущностью и DTO.</li>
 * </ul>
 *
 * <p>Ключевые проверки:</p>
 * <ul>
 *   <li>Нельзя комментировать собственное событие;</li>
 *   <li>Нельзя комментировать событие без подтверждённой заявки;</li>
 *   <li>Редактировать/удалять можно только свой комментарий (исключение {@link ru.practicum.exception.InitiatorRequestException});</li>
 *   <li>Работа только с опубликованными событиями ({@link ru.practicum.dto.EventState#PUBLISHED}).</li>
 * </ul>
 *
 * <p>Все операции логируются через {@code Slf4j}. В случае ошибок сервис выбрасывает
 * {@link ru.practicum.exception.EntityNotFoundException}, {@link ru.practicum.exception.ValidationException}
 * или {@link ru.practicum.exception.InitiatorRequestException}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserFeign userFeign;
    private final EventRepository eventRepository;
    private final RequestFeign requestFeign;
    private final CommentMapper commentMapper;


    @Override
    public CommentDto privateAdd(Long userId, Long eventId, InputCommentDto inputCommentDto) {
        Event event = findEvent(eventId);
        if (event.getInitiatorId().equals(userId)) {
            throw new ValidationException(Comment.class, " Нельзя оставлять комментарии к своему событию.");
        }
        if (requestFeign.findByRequesterIdAndEventId(userId, eventId).isEmpty()) {
            throw new ValidationException(Comment.class, " Пользователь с ID - " + userId + ", не заявился на событие с ID - " + eventId + ".");
        }
        UserShortDto author = findUser(userId);

        Comment comment = commentMapper.toComment(inputCommentDto, author, event);
        log.info("Результат маппинга: {}", comment);
        comment.setCreated(LocalDateTime.now());
        comment = commentRepository.save(comment);
        log.info("Сохраняем комментарий в БД: {}", comment);

        CommentDto dto = commentMapper.toCommentDto(comment, author);
        log.info("Результат маппинга: {}", dto);
        return dto;
    }

    @Override
    public void privateDelete(Long userId, Long commentId) {
        UserShortDto author = findUser(userId);
        Comment comment = findComment(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new InitiatorRequestException(" Нельзя удалить комментарий другого пользователя.");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public void adminDelete(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public CommentDto privateUpdate(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        UserShortDto author = findUser(userId);
        Comment comment = findComment(commentId);
        if (!comment.getAuthorId().equals(userId)) {
            throw new InitiatorRequestException(" Нельзя редактировать комментарий другого пользователя.");
        }
        comment.setText(updateCommentDto.getText());
        comment = commentRepository.save(comment);
        log.info("Обновляем комментарий в БД: {}", comment);

        CommentDto dto = commentMapper.toCommentDto(comment, author);
        log.info("Результат маппинга: {}", dto);
        return dto;
    }

    @Override
    public CommentDto adminUpdate(Long id, UpdateCommentDto updateCommentDto) {

        Comment comment = findComment(id);

        if (updateCommentDto.getText() != null) {
            comment.setText(updateCommentDto.getText());
        }

        comment = commentRepository.save(comment);
        log.info("Обновляем комментарий в БД: {}", comment);
        UserShortDto author = findUser(comment.getAuthorId());

        CommentDto dto = commentMapper.toCommentDto(comment, author);
        log.info("Результат маппинга: {}", dto);
        return dto;
    }

    @Override
    public List<CommentDto> findCommentsByEventId(Long eventId, Integer from, Integer size) {
        Event event = findEvent(eventId);
        Pageable pageable = PageRequest.of(from, size);
        List<Comment> comments = commentRepository.findAllByEventId(eventId, pageable);
        log.info("Найдены комментарии: {}", comments);

        if (!comments.isEmpty()) {
            return addUsersShortDto(comments);
        }
        return new ArrayList<>();
    }

    @Override
    public CommentDto findCommentById(Long commentId) {
        Comment comment = findComment(commentId);
        UserShortDto author = findUser(comment.getAuthorId());
        return commentMapper.toCommentDto(comment, author);
    }

    @Override
    public List<CommentDto> findCommentsByEventIdAndUserId(Long eventId, Long userId, Integer from, Integer size) {
        Event event = findEvent(eventId);
        Pageable pageable = PageRequest.of(from, size);
        List<Comment> comments = commentRepository.findAllByEventIdAndAuthorId(eventId, userId, pageable);
        if (!comments.isEmpty()) {
            return addUsersShortDto(comments);
        }
        return new ArrayList<>();
    }

    @Override
    public List<CommentDto> findCommentsByUserId(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        List<Comment> comments = commentRepository.findAllByAuthorId(userId, pageable);
        if (!comments.isEmpty()) {
            return addUsersShortDto(comments);
        }
        return new ArrayList<>();
    }

    private Event findEvent(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException(
                        Event.class, "Событие c ID - " + eventId + ", не найдено или ещё не опубликовано"));
        log.info("Найдено событие: {}", event);
        return event;
    }

    private UserShortDto findUser(Long userId) {
        try {
            UserShortDto dto = userFeign.findUserShortDtoById(userId);
            log.info("Результат поиска user-service: {}", dto);
            return dto;
        } catch (FeignException e) {
            throw new EntityNotFoundException(UserShortDto.class, "Пользователь c ID - " + userId + ", не найден.");
        }
    }

    private Comment findComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, "Комментарий c ID - " + commentId + ", не найден."));
        log.info("Найден коментарий: {}", comment);
        return comment;
    }

    private List<CommentDto> addUsersShortDto(List<Comment> comments) {
        Set<Long> usersId = comments.stream().map(Comment::getAuthorId).collect(Collectors.toSet());
        Map<Long, UserShortDto> users;
        try {
            users = userFeign.findUserShortDtoById(usersId);
            log.info("Результат поиска user-service: {}", users);
        } catch (FeignException e) {
            throw new EntityNotFoundException(UserShortDto.class, "Пользователи не найдены.");
        }
        List<CommentDto> result = new ArrayList<>();
        for (Comment comment : comments) {
            UserShortDto author = users.get(comment.getAuthorId());
            CommentDto dto = commentMapper.toCommentDto(comment, author);
            result.add(dto);
        }
        log.info("Результат добавления автора комментария: {}", result);
        return result;
    }
}