package ru.practicum.common.dto.events;

public interface Requestable {
    Long getId();
    Long getConfirmedRequests();
    void setConfirmedRequests(Long confirmedRequests);
}