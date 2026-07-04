package ru.practicum.compilation_service.mapper;

import ru.practicum.common.compilationDto.dto.CompilationDto;
import ru.practicum.common.compilationDto.dto.NewCompilationDto;
import ru.practicum.common.compilationDto.dto.UpdateCompilationRequest;
import ru.practicum.common.eventDto.dto.EventShortDto;
import ru.practicum.compilation_service.model.Compilation;

import java.util.ArrayList;
import java.util.List;

public class CompilationMapper {

    public static Compilation toCompilation(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned() != null && dto.getPinned())
                .eventId(dto.getEvents() != null ?
                        new ArrayList<>(dto.getEvents()) :
                        new ArrayList<>())
                .build();
    }

    public static void updateCompilationFromRequest(UpdateCompilationRequest request, Compilation compilation) {
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
            compilation.setEventId(new ArrayList<>(request.getEvents()));
        }
    }

    public static CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPinned())
                .events(events)
                .build();
    }
}