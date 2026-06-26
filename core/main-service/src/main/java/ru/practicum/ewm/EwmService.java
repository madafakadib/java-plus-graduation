package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.practicum.stat.client.StatsClient;

@SpringBootApplication
@Import(StatsClient.class)
@EntityScan("ru.practicum.ewm")
@EnableJpaRepositories("ru.practicum.ewm")
public class EwmService {
    public static void main(String[] args) {
        SpringApplication.run(EwmService.class, args);
    }
}