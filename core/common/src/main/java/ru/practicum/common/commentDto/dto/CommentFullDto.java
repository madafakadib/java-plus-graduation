package ru.practicum.common.commentDto.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.common.userDto.dto.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentFullDto {
    private Long id;
    private String text;
    private Long authorId;
    private Long eventId;
    private LocalDateTime created;
    private LocalDateTime updated;
    private String status;
    private Long moderatorId;
    private LocalDateTime moderatedAt;
}
