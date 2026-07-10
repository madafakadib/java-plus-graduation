package ru.practicum.category.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
/**
 * JPA-сущность категории событий.
 * <p>Отображается на таблицу {@code categories}.</p>
 *
 * <ul>
 *   <li>{@code id} — первичный ключ, автоинкремент;</li>
 *   <li>{@code name} — уникальное и обязательное название категории.</li>
 * </ul>
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@ToString
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}