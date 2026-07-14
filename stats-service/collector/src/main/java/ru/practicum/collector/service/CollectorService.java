package ru.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.collector.kafka.KafkaClient;
import ru.practicum.collector.mapper.UserActionMapper;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.messages.UserActionProto;

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
}