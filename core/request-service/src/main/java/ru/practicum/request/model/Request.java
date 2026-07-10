package ru.practicum.request.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.dto.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@ToString
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "event_id")
    private Long eventId;
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;
    @Column(name = "created_at")
    private LocalDateTime createdOn;
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}