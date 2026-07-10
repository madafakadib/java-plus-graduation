package ru.practicum.commentsService.comments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {
        "ru.practicum.commentsService",
        "ru.practicum.common"
})
public class CommentsService {
    public static void main(String[] args) {
        SpringApplication.run(CommentsService.class, args);
    }
}