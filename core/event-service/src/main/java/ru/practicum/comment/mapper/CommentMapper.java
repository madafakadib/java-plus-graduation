package ru.practicum.comment.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.InputCommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.dto.UserShortDto;
import ru.practicum.event.model.Event;

import java.util.List;
/**
 * MapStruct-мэппер для преобразования комментариев между сущностью и DTO.
 * <p>Реализация генерируется MapStruct, бин регистрируется в Spring.</p>
 *
 * <ul>
 *   <li>{@link #toComment(InputCommentDto, ru.practicum.dto.UserShortDto, ru.practicum.event.model.Event)} —
 *       создаёт новую сущность {@link Comment} из входного DTO, автора и события;</li>
 *   <li>{@link #toCommentDto(Comment, ru.practicum.dto.UserShortDto)} —
 *       преобразует сущность в {@link CommentDto}, включая краткие данные автора;</li>
 *   <li>{@link #toComment(CommentDto, ru.practicum.event.model.Event)} —
 *       создаёт сущность из полного DTO и события (метка времени выставляется как {@code now()});</li>
 *   <li>{@link #toCommentDtos(List)} — преобразует список сущностей в список DTO (без автора).</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "created", ignore = true)
    Comment toComment(InputCommentDto inputCommentDto, UserShortDto author, Event event);

    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "eventId", source = "comment.event.id")
    @Mapping(target = "author", source = "author")
    CommentDto toCommentDto(Comment comment, UserShortDto author);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", source = "commentDto.author.id")
    @Mapping(target = "event", source = "event")
    @Mapping(target = "created", expression = "java(java.time.LocalDateTime.now())")
    Comment toComment(CommentDto commentDto, Event event);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "author", ignore = true)
    List<CommentDto> toCommentDtos(List<Comment> comments);
}