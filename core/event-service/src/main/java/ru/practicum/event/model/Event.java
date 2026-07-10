package ru.practicum.event.model;


import jakarta.persistence.*;
import lombok.*;
import ru.practicum.dto.EventState;

import java.time.LocalDateTime;
/**
 * JPA-сущность события.
 * <p>Отображается на таблицу {@code events}.</p>
 *
 * <ul>
 *   <li>{@code id} — первичный ключ, автоинкремент;</li>
 *   <li>{@code annotation} — краткая аннотация события;</li>
 *   <li>{@code categoryId} — идентификатор категории (внешний ключ на {@code categories});</li>
 *   <li>{@code createdOn} — дата/время создания события;</li>
 *   <li>{@code description} — полное описание события;</li>
 *   <li>{@code eventDate} — дата/время проведения события;</li>
 *   <li>{@code initiatorId} — идентификатор инициатора (внешний ключ на {@code users});</li>
 *   <li>{@code location} — ссылка на локацию события ({@link Location});</li>
 *   <li>{@code paid} — признак платного участия;</li>
 *   <li>{@code participantLimit} — лимит участников (0 = без ограничений);</li>
 *   <li>{@code publishedOn} — дата/время публикации события;</li>
 *   <li>{@code requestModeration} — нужно ли подтверждать заявки на участие;</li>
 *   <li>{@code state} — текущее состояние события ({@link ru.practicum.dto.EventState});</li>
 *   <li>{@code title} — заголовок события.</li>
 * </ul>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String annotation;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "created_on")
    private LocalDateTime createdOn;
    private String description;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "initiator_id")
    private Long initiatorId;
    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;
    private Boolean paid;

    @Column(name = "participant_limit")
    private Long participantLimit;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private EventState state;
    private String title;
}