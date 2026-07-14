package ru.practicum.common.dto.events;

public interface Commentable {
    Long getId();
    Long getCommentsCount();
    void setCommentsCount(Long commentsCount);
}