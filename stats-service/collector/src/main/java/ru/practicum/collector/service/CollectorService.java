package ru.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.collector.kafka.KafkaClient;
import ru.practicum.collector.mapper.UserActionMapper;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.messages.UserActionProto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorService {
    private final KafkaClient kafkaClient;
    private final UserActionMapper userActionMapper;

    @Value("${kafka.topics.user-actions}")
    private String userActionTopic;

    public void collectUserAction(UserActionProto request) {
        UserActionAvro actionAvro = userActionMapper.mapToAvro(request);
        kafkaClient.send(
                userActionTopic,
                actionAvro.getTimestamp(),
                actionAvro.getEventId(),
                actionAvro);
    }

    public void collectUserActionsBatch(List<UserActionProto> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }
        for (UserActionProto request : requests) {
            try {
                collectUserAction(request);
            } catch (Exception e) {
                log.error("Ошибка при отправке в кафка: userId={}, eventId={}",
                        request.getUserId(), request.getEventId(), e);
            }
        }
    }
}