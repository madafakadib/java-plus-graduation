package ru.practicum.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.event.model.Location;

import java.util.Optional;
/**
 * Spring Data JPA-репозиторий для работы с локациями событий ({@link ru.practicum.event.model.Location}).
 * <p>Предоставляет стандартные CRUD-операции и дополнительный метод поиска.</p>
 *
 * <ul>
 *   <li>{@link #findByLatAndLon(Float, Float)} —
 *       поиск локации по координатам широты и долготы.</li>
 * </ul>
 *
 * <p>Используется сервисным слоем при сохранении и повторном использовании координат событий.</p>
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    Optional<Location> findByLatAndLon(Float lat, Float lon);
}