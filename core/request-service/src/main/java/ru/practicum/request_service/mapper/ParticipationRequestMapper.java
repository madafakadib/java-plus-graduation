package ru.practicum.request_service.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.common.requestDto.dto.EventRequestStatusUpdateResult;
import ru.practicum.common.requestDto.dto.ParticipationRequestDto;
import ru.practicum.request_service.model.ParticipationRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ParticipationRequestMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ParticipationRequest toParticipationRequest(Long eventId, Long requesterId) {
        return ParticipationRequest.builder()
                .eventId(eventId)
                .requesterId(requesterId)
                .created(LocalDateTime.now())
                .status(ru.practicum.common.requestDto.model.RequestStatus.PENDING)
                .build();
    }

    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        if (request == null) {
            return null;
        }

        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated() != null ? request.getCreated().format(FORMATTER) : null)
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus())
                .build();
    }

    public static List<ParticipationRequestDto> toParticipationRequestDto(List<ParticipationRequest> requests) {
        return requests.stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    public static EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(
            List<ParticipationRequest> confirmedRequests,
            List<ParticipationRequest> rejectedRequests) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(toParticipationRequestDto(confirmedRequests))
                .rejectedRequests(toParticipationRequestDto(rejectedRequests))
                .build();
    }
}