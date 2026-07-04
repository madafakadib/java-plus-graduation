package ru.practicum.compilation_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.client.EventClient;
import ru.practicum.common.compilationDto.dto.CompilationDto;
import ru.practicum.common.compilationDto.dto.NewCompilationDto;
import ru.practicum.common.compilationDto.dto.UpdateCompilationRequest;
import ru.practicum.common.eventDto.dto.EventShortDto;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.compilation_service.mapper.CompilationMapper;
import ru.practicum.compilation_service.model.Compilation;
import ru.practicum.compilation_service.repository.CompilationRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventClient eventClient;

    @Override
    @Transactional
    public CompilationDto postCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = Compilation.builder()
                .pinned(newCompilationDto.getPinned() != null ? newCompilationDto.getPinned() : false)
                .title(newCompilationDto.getTitle())
                .eventId(new ArrayList<>())
                .build();

        List<Long> eventIds = newCompilationDto.getEvents();
        if (eventIds != null && !eventIds.isEmpty()) {
            for (Long eventId : eventIds) {
                if (!eventClient.existsById(eventId)) {
                    throw new NotFoundException("Event with id=" + eventId + " was not found");
                }
            }
            compilation.setEventId(new ArrayList<>(eventIds));
        }

        Compilation saved = compilationRepository.postCompilation(compilation);

        List<EventShortDto> eventDtos = getEventShortDtos(saved.getEventId());

        return CompilationMapper.toCompilationDto(saved, eventDtos);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка не найдена");
        }
        compilationRepository.deleteCompilation(compId);
    }

    @Override
    @Transactional
    public CompilationDto patchCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = compilationRepository.getCompilationById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }
        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isBlank()) {
            compilation.setTitle(updateRequest.getTitle());
        }
        if (updateRequest.getEvents() != null) {
            for (Long eventId : updateRequest.getEvents()) {
                if (!eventClient.existsById(eventId)) {
                    throw new NotFoundException("Event with id=" + eventId + " was not found");
                }
            }
            compilation.setEventId(new ArrayList<>(updateRequest.getEvents()));  // ← eventIds
        }

        Compilation updated = compilationRepository.patchCompilation(compId, compilation);

        List<EventShortDto> eventDtos = getEventShortDtos(updated.getEventId());

        return CompilationMapper.toCompilationDto(updated, eventDtos);
    }


    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Compilation> compilations = compilationRepository.getCompilations(pinned, from, size);

        if (compilations.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> allEventIds = compilations.stream()
                .flatMap(c -> c.getEventId().stream())
                .collect(Collectors.toSet());

        List<EventShortDto> allEvents = getEventShortDtos(new ArrayList<>(allEventIds));
        Map<Long, EventShortDto> eventsMap = allEvents.stream()
                .collect(Collectors.toMap(EventShortDto::getId, e -> e));

        return compilations.stream()
                .map(c -> {
                    List<EventShortDto> compEvents = c.getEventId().stream()
                            .map(eventsMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                    return CompilationMapper.toCompilationDto(c, compEvents);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.getCompilationById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));

        List<EventShortDto> eventDtos = getEventShortDtos(compilation.getEventId());

        return CompilationMapper.toCompilationDto(compilation, eventDtos);
    }

    private List<EventShortDto> getEventShortDtos(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        return eventClient.getShortDtosByIds(eventIds);
    }
}
