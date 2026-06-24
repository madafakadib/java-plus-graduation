package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.practicum.stat.client.StatsClient;

@SpringBootApplication
@Import(StatsClient.class)
public class EwmService {
    public static void main(String[] args) {
        SpringApplication.run(EwmService.class, args);
    }
}