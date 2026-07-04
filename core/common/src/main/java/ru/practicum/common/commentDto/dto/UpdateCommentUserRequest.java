package ru.practicum.common.commentDto.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.common.commentDto.enums.CommentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentUserRequest {
    @Size(max = 2000)
    private String text;

    private CommentStatus status;
}