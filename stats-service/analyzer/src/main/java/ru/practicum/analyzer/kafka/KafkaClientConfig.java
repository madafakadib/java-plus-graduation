package ru.practicum.analyzer.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
public class KafkaClientConfig {

    @Bean
    @ConfigurationProperties(prefix = "analyzer.kafka.consumer.user-action.properties")
    public Properties getKafkaConsumerUserActionProperties() {
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "analyzer.kafka.consumer.event-similarity.properties")
    public Properties getKafkaConsumerEventSimilarityProperties() {
        return new Properties();
    }

    @Bean
    KafkaClient getKafkaClient() {
        return new KafkaClient() {
            private Consumer<Long, UserActionAvro> kafkaUserActionConsumer;
            private Consumer<Long, EventSimilarityAvro> kafkaEventSimilarityConsumer;

            @Override
            public Consumer<Long, UserActionAvro> getKafkaUserActionConsumer() {
                kafkaUserActionConsumer = new KafkaConsumer<>(getKafkaConsumerUserActionProperties());
                return kafkaUserActionConsumer;
            }

            @Override
            public Consumer<Long, EventSimilarityAvro> getKafkaEventSimilarityConsumer() {
                kafkaEventSimilarityConsumer = new KafkaConsumer<>(getKafkaConsumerEventSimilarityProperties());
                return kafkaEventSimilarityConsumer;
            }

            @Override
            public void close() {
                try {
                    kafkaUserActionConsumer.commitSync();
                    kafkaEventSimilarityConsumer.commitSync();
                } finally {
                    kafkaUserActionConsumer.close();
                    kafkaEventSimilarityConsumer.close();
                }
            }
        };
    }
}