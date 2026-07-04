package ru.practicum.common.commentDto.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCountDto {
    private Long eventId;
    private Long count;
}