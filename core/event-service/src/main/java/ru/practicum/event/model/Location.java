package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
/**
 * JPA-сущность географической локации события.
 * <p>Отображается на таблицу {@code locations}.</p>
 *
 * <ul>
 *   <li>{@code id} — первичный ключ, автоинкремент;</li>
 *   <li>{@code lat} — широта (latitude);</li>
 *   <li>{@code lon} — долгота (longitude).</li>
 * </ul>
 *
 * <p>Связана с {@link Event} через {@code @ManyToOne}.</p>
 */
@Entity
@Table(name = "locations")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Float lat;
    private Float lon;
}
