package ru.practicum.event.mapper;


import org.mapstruct.Mapper;
import ru.practicum.dto.LocationDto;
import ru.practicum.event.model.Location;
/**
 * MapStruct-мэппер для преобразования координат события между сущностью и DTO.
 * <p>Реализация генерируется MapStruct, бин регистрируется в Spring.</p>
 *
 * <ul>
 *   <li>{@link #toLocation(ru.practicum.dto.LocationDto)} —
 *       преобразует DTO {@link ru.practicum.dto.LocationDto} в JPA-сущность {@link ru.practicum.event.model.Location};</li>
 *   <li>{@link #toLocationDto(ru.practicum.event.model.Location)} —
 *       преобразует сущность {@link ru.practicum.event.model.Location} в DTO {@link ru.practicum.dto.LocationDto}.</li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface LocationMapper {

    Location toLocation(LocationDto locationDto);

    LocationDto toLocationDto(Location location);

}
