package ru.practicum.event.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.practicum.dto.Constants.FORMAT_DATETIME;

/**
 * MapStruct-мэппер для преобразования событий между сущностью и DTO.
 * <p>Реализация генерируется MapStruct, бин регистрируется в Spring.</p>
 *
 * <ul>
 *   <li>{@link #toEventShortDto(List)} —
 *       преобразует список {@link ru.practicum.event.model.Event} в список {@link ru.practicum.dto.EventShortDto};</li>
 *   <li>{@link #toEventFullDto(Event)} —
 *       преобразует событие в {@link ru.practicum.dto.EventFullDto} без категории и инициатора;</li>
 *   <li>{@link #toEventFullDto(Event, ru.practicum.dto.CategoryDto)} —
 *       преобразует событие в {@link ru.practicum.dto.EventFullDto}, включая категорию;</li>
 *   <li>{@link #toEventFullDto(Event, ru.practicum.dto.UserShortDto)} —
 *       преобразует событие в {@link ru.practicum.dto.EventFullDto}, включая инициатора;</li>
 *   <li>{@link #toEventFullDto(Event, ru.practicum.dto.UserShortDto, ru.practicum.dto.CategoryDto)} —
 *       преобразует событие в {@link ru.practicum.dto.EventFullDto}, включая и категорию, и инициатора;</li>
 *   <li>{@link #toEventFullDtos(List)} —
 *       преобразует список событий в список {@link ru.practicum.dto.EventFullDto};</li>
 *   <li>{@link #toEvent(ru.practicum.event.dto.NewEventDto)} —
 *       создаёт сущность {@link ru.practicum.event.model.Event} из {@link ru.practicum.event.dto.NewEventDto}
 *       (часть полей игнорируется и будет заполнена сервисным слоем);</li>
 *   <li>{@link #toEventShortDtos(List)} —
 *       преобразует список {@link ru.practicum.dto.EventFullDto} в список {@link ru.practicum.dto.EventShortDto}.</li>
 * </ul>
 *
 * <p>Дополнительно содержит мапперы по умолчанию для преобразования даты:</p>
 * <ul>
 *   <li>{@link #stringToLocalDateTime(String)} — строка → {@link java.time.LocalDateTime};</li>
 *   <li>{@link #localDateTimeToString(LocalDateTime)} — {@link java.time.LocalDateTime} → строка.</li>
 * </ul>
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    List<EventShortDto> toEventShortDto(List<Event> event);

    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "category", source = "categoryDto")
    @Mapping(target = "initiator", ignore = true)
    EventFullDto toEventFullDto(Event event, CategoryDto categoryDto);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", source = "userShortDto")
    EventFullDto toEventFullDto(Event event, UserShortDto userShortDto);

    @Mapping(target = "id", source = "event.id")
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "initiator", source = "userShortDto")
    @Mapping(target = "category", source = "categoryDto")
    EventFullDto toEventFullDto(Event event, UserShortDto userShortDto, CategoryDto categoryDto);

    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    List<EventFullDto> toEventFullDtos(List<Event> events);

    @Mapping(target = "categoryId", source = "category")
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "eventDate", source = "eventDate")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "state", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    List<EventShortDto> toEventShortDtos(List<EventFullDto> eventFullDtos);

    default LocalDateTime stringToLocalDateTime(String stringDate) {
        if (stringDate == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATETIME);
        return LocalDateTime.parse(stringDate, formatter);
    }

    default String localDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMAT_DATETIME);
        return localDateTime.format(formatter);
    }
}