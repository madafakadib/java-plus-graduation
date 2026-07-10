package ru.practicum.compilation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.dto.EventShortDto;

import java.util.List;
/**
 * MapStruct-мэппер для преобразования подборок событий между сущностью и DTO.
 * <p>Реализация генерируется MapStruct, бин регистрируется в Spring.</p>
 *
 * <ul>
 *   <li>{@link #toCompilation(CompilationDto)} —
 *       преобразует {@link CompilationDto} в сущность {@link ru.practicum.compilation.model.Compilation};</li>
 *   <li>{@link #toCompilationDto(Compilation, List)} —
 *       преобразует сущность в {@link CompilationDto}, включая связанные события
 *       (список {@link ru.practicum.dto.EventShortDto} передаётся отдельно);</li>
 *   <li>{@link #toCompilationDtos(List)} —
 *       преобразует список сущностей в список DTO без событий
 *       (поле {@code events} игнорируется).</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface CompilationMapper {

    Compilation toCompilation(CompilationDto compilationDto);

    @Mapping(target = "events", source = "eventDtos")
    CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> eventDtos);

    @Mapping(target = "events", ignore = true)
    List<CompilationDto> toCompilationDtos(List<Compilation> compilations);
}