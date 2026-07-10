package ru.practicum.commentsService.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.commentsService.comments.repository.CommentRepository;
import ru.practicum.common.dto.comments.CommentStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CommentInternalServiceImpl implements CommentInternalService {

    private final CommentRepository commentRepository;

    @Override
    public Map<Long, Long> getCommentCountsByEventIds(List<Long> eventIds, CommentStatus status) {

        List<Object[]> counts = commentRepository.countByEventIdInAndStatus(eventIds, status);
        return counts.stream()
            .collect(Collectors.toMap(
                    arr -> (Long) arr[0],
                    arr -> (Long) arr[1]
            ));

    }
}
