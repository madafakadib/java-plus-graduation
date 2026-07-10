package ru.practicum.eventsService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {
        "ru.practicum.eventsService",
        "ru.practicum.stat",
        "ru.practicum.common"
})
public class EventsService {
    public static void main(String[] args) {
        SpringApplication.run(EventsService.class, args);
    }
}