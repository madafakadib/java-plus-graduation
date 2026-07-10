package ru.practicum.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
/**
 * DTO для создания новой категории.
 * <p>Используется в админском API при {@code POST /admin/categories}.</p>
 *
 * <ul>
 *   <li>{@code name} — обязательное, длина от 1 до 50 символов.</li>
 * </ul>
 */
@Data
public class NewCategoryDto {
    @NotBlank
    @Size(min = 1, max = 50)
    private String name;
}