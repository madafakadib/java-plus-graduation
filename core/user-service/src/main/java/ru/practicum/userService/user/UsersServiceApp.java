package ru.practicum.userService.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "ru.practicum.userService.user",
        "ru.practicum.common"
})
@EnableFeignClients
public class UsersServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(UsersServiceApp.class, args);
    }
}