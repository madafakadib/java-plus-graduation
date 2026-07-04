package ru.practicum.event_service.repository;


import ru.practicum.common.eventDto.dto.EventFullDto;
import ru.practicum.common.eventDto.dto.EventShortDto;
import ru.practicum.common.eventDto.dto.paramDto.EventRepositoryParam;

import java.util.List;
import java.util.Optional;

public interface EventRepositoryCustom {

    List<EventShortDto> findEventsShortDto(EventRepositoryParam param);

    Optional<EventFullDto> findEventByIdFullDto(Long id);

    List<EventFullDto> findEventsFullDto(EventRepositoryParam param);
}
