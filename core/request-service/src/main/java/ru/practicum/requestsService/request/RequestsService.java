package ru.practicum.requestsService.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {
        "ru.practicum.requestsService.request",
        "ru.practicum.common"
})
public class RequestsService {
    public static void main(String[] args) {
        SpringApplication.run(RequestsService.class, args);
    }
}