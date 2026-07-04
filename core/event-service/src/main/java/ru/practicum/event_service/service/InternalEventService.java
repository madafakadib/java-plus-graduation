package ru.practicum.event_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.common.client.CategoryClient;
import ru.practicum.common.client.UserClient;
import ru.practicum.common.eventDto.dto.EventFullDto;
import ru.practicum.common.eventDto.dto.EventShortDto;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.event_service.mapper.EventMapper;
import ru.practicum.event_service.model.Event;
import ru.practicum.event_service.repository.EventRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalEventService {

    private final EventRepository eventRepository;
    private final CategoryClient categoryClient;
    private final UserClient userClient;

    public boolean existsByCategoryId(Long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    public boolean existsById(Long eventId) {
        return eventRepository.existsById(eventId);
    }

    public EventFullDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        var category = categoryClient.getCategoryById(event.getCategoryId());
        var initiator = userClient.getUserShort(event.getInitiatorId());

        return EventMapper.toEventFullDto(event, 0L, 0L, category, initiator);
    }

    public List<EventShortDto> getShortDtosByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = eventRepository.findAllById(ids);

        return events.stream()
                .map(event -> {
                    var category = categoryClient.getCategoryById(event.getCategoryId());
                    var initiator = userClient.getUserShort(event.getInitiatorId());
                    return EventMapper.toEventShortDto(event, 0L, 0L, category, initiator);
                })
                .collect(Collectors.toList());
    }
}