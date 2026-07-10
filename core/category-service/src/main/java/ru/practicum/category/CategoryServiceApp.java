package ru.practicum.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
/**
 * Точка входа микросервиса категорий.
 * <p>Запускает Spring Boot-приложение и включает поддержку Feign-клиентов
 * для взаимодействия с сервисом событий.</p>
 *
 * <p>Основные компоненты:</p>
 * <ul>
 *   <li>REST-контроллеры (публичный и административный API);</li>
 *   <li>Сервисный слой {@code CategoryService};</li>
 *   <li>Интеграция с event-service через Feign.</li>
 * </ul>
 */
@SpringBootApplication
@EnableFeignClients
public class CategoryServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CategoryServiceApp.class, args);
    }
}