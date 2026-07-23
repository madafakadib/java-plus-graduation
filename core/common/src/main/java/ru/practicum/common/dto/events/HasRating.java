package ru.practicum.common.dto.events;

public interface HasRating {
    Long getId();
    Double getRating();
    void setRating(Double rating);
}