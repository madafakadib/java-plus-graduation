package ru.practicum.analyzer.processor;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.handler.EventSimilarityHandler;
import ru.practicum.analyzer.kafka.KafkaClient;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EventSimilarityProcessor implements Runnable {
    private final Consumer<Long, EventSimilarityAvro> similarityConsumer;
    private final EventSimilarityHandler similarityHandler;

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);


    @Value("${analyzer.kafka.topics.events-similarity}")
    private String eventsSimilarityTopic;

    public EventSimilarityProcessor(KafkaClient kafkaClient, EventSimilarityHandler similarityHandler) {
        this.similarityConsumer = kafkaClient.getKafkaEventSimilarityConsumer();
        this.similarityHandler = similarityHandler;
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(similarityConsumer::wakeup));
        try {
            similarityConsumer.subscribe(List.of(eventsSimilarityTopic));
            while (true) {
                ConsumerRecords<Long, EventSimilarityAvro> records = similarityConsumer.poll(CONSUME_ATTEMPT_TIMEOUT);
                if (!records.isEmpty()) {
                    int count = 0;
                    for (ConsumerRecord<Long, EventSimilarityAvro> record : records) {
                        EventSimilarityAvro avro = record.value();

                        similarityHandler.handleEventSimilarity(avro);

                        manageOffsets(record, count, similarityConsumer);
                        count++;
                    }
                    similarityConsumer.commitAsync();
                }
            }
        } catch (WakeupException ignored) {

        } finally {
            try {
                similarityConsumer.commitSync();
            } finally {
                similarityConsumer.close();
            }
        }
    }

    private static void manageOffsets(
            ConsumerRecord<Long, EventSimilarityAvro> record,
            int count,
            Consumer<Long, EventSimilarityAvro> consumer
    ) {

        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
            });
        }
    }
}