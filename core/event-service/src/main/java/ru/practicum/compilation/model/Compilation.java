package ru.practicum.compilation.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.event.model.Event;

import java.util.List;
/**
 * JPA-сущность подборки событий.
 * <p>Отображается на таблицу {@code compilations}.</p>
 *
 * <ul>
 *   <li>{@code id} — первичный ключ, автоинкремент;</li>
 *   <li>{@code events} — список событий, включённых в подборку
 *       (связь многие-ко-многим через таблицу {@code compilation_events});</li>
 *   <li>{@code pinned} — признак закреплённой подборки на главной странице;</li>
 *   <li>{@code title} — название подборки, обязательное.</li>
 * </ul>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private List<Event> events;

    @Column(name = "pinned", nullable = false)
    private Boolean pinned;

    @Column(name = "title", nullable = false)
    private String title;
}