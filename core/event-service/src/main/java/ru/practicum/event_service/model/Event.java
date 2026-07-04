package ru.practicum.event_service.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.common.eventDto.dto.enums.EventState;
import ru.practicum.common.eventDto.dto.model.Location;

import java.time.LocalDateTime;

@Entity
@Table(name = "events", schema = "events")
@Builder
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)  //  безопасно для @OneToMany и lazy-загрузок
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA обязан иметь конструктор без аргументов
@AllArgsConstructor  // для Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(nullable = false)
    private Boolean paid;

    @Column(name = "participant_limit", nullable = false)
    private Integer participantLimit; // (0 - нет лимита)

    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration;  // нужна ли пре-модерация заявок

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventState state;


    @Column(name = "initiator_id", nullable = false)
    private Long initiatorId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Embedded
    private Location location;
}