package ru.practicum.ewm.request.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ParticipationRequestMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ParticipationRequest toParticipationRequest(Event event, User requester) {
        return ParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .created(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .build();
    }

    public ParticipationRequestDto toParticipationRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .created(request.getCreated().format(FORMATTER))
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }

    public List<ParticipationRequestDto> toParticipationRequestDto(List<ParticipationRequest> requests) {
        return requests.stream()
                .map(this::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    public EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(
            List<ParticipationRequest> confirmedRequests,
            List<ParticipationRequest> rejectedRequests) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(toParticipationRequestDto(confirmedRequests))
                .rejectedRequests(toParticipationRequestDto(rejectedRequests))
                .build();
    }
}