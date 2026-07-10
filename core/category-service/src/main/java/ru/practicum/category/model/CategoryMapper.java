package ru.practicum.category.model;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.category.dto.NewCategoryDto;
import ru.practicum.dto.CategoryDto;

import java.util.List;
/**
 * MapStruct-мэппер между сущностью {@link Category} и DTO.
 * <p>Используется для преобразования данных при работе с REST-слоем.</p>
 *
 * <ul>
 *   <li>{@link #toCategoryDtoList(List)} — список сущностей → список DTO;</li>
 *   <li>{@link #toCategoryDto(Category)} — сущность → DTO;</li>
 *   <li>{@link #toCategoryByNew(NewCategoryDto)} — создание новой сущности из входного DTO
 *       (поле {@code id} игнорируется).</li>
 * </ul>
 *
 * <p>Реализация генерируется MapStruct, бин регистрируется в Spring.</p>
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {
    List<CategoryDto> toCategoryDtoList(List<Category> categories);

    CategoryDto toCategoryDto(Category category);

    @Mapping(target = "id", ignore = true)
    Category toCategoryByNew(NewCategoryDto newCategoryDto);
}