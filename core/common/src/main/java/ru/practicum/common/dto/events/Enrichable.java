package ru.practicum.common.dto.events;

import ru.practicum.common.dto.users.UserShortDto;

public interface Enrichable extends HasRating, Requestable, Commentable {
    UserShortDto getInitiator();
    void setInitiator(UserShortDto initiator);
}