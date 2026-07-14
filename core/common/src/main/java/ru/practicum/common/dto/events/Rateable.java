package ru.practicum.common.dto.events;

public interface Rateable {
    Long getId();
    Double getRating();
    void setRating(Double rating);
}