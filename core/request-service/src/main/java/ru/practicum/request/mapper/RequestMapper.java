package ru.practicum.request.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.request.model.Request;

import java.util.List;

import static ru.practicum.dto.Constants.FORMAT_DATETIME;


@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "created", source = "createdOn", dateFormat = FORMAT_DATETIME)
    ParticipationRequestDto toParticipationRequestDto(Request request);

    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "requester", source = "requesterId")
    @Mapping(target = "created", source = "createdOn", dateFormat = FORMAT_DATETIME)
    List<ParticipationRequestDto> toParticipationRequestDto(List<Request> requests);
}