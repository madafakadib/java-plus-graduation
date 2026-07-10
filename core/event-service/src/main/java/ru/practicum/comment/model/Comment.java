package ru.practicum.comment.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
/**
 * JPA-сущность комментария к событию.
 * <p>Отображается на таблицу {@code comments}.</p>
 *
 * <ul>
 *   <li>{@code id} — первичный ключ, автоинкремент;</li>
 *   <li>{@code event} — ссылка на событие, к которому относится комментарий;</li>
 *   <li>{@code authorId} — идентификатор пользователя-автора;</li>
 *   <li>{@code text} — текст комментария, не пустой;</li>
 *   <li>{@code created} — дата и время создания комментария.</li>
 * </ul>
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
@ToString
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    @Column(name = "text", nullable = false)
    private String text;
    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}