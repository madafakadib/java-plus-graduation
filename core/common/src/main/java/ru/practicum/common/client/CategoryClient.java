package ru.practicum.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.common.categoryDto.dto.CategoryDto;

@FeignClient(name = "category-service", path = "/api/internal/categories")
public interface CategoryClient {

    @GetMapping("/{categoryId}")
    CategoryDto getCategoryById(@PathVariable("categoryId") Long categoryId);
}