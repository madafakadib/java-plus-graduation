package ru.practicum.collector.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.Instant;
import java.util.Properties;

@Configuration
public class KafkaClientConfig {

    @Bean
    @ConfigurationProperties(prefix = "kafka.producer.properties")
    public Properties kafkaProducerProperties() {
        return new Properties();
    }

    @Bean
    Producer<Long, SpecificRecordBase> kafkaProducer(Properties kafkaProducerProperties) {
        return new KafkaProducer<>(kafkaProducerProperties);
    }

    @Bean
    KafkaClient getKafkaClient(Producer<Long, SpecificRecordBase> kafkaProducer) {
        return new KafkaClient() {
            @Override
            public void send(String topic, Instant timestamp, Long eventId, SpecificRecordBase event) {
                ProducerRecord<Long, SpecificRecordBase> record =
                        new ProducerRecord<>(topic, null, timestamp.toEpochMilli(), eventId, event);
                try {
                    kafkaProducer.send(record, (metadata, exception) -> {
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void close() {
                kafkaProducer.flush();
                kafkaProducer.close(Duration.ofSeconds(10));
            }
        };
    }
}