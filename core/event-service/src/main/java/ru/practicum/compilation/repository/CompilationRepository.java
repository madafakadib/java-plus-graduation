package ru.practicum.compilation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.compilation.model.Compilation;

import java.util.List;
/**
 * Spring Data JPA-репозиторий для сущностей {@link ru.practicum.compilation.model.Compilation}.
 * <p>Предоставляет стандартные CRUD-операции через {@link JpaRepository} и
 * дополнительный метод выборки по признаку закреплённости.</p>
 *
 * <ul>
 *   <li>{@link #findAllByPinned(Boolean, org.springframework.data.domain.Pageable)} —
 *       возвращает список подборок с постраничной выборкой. Если параметр {@code pinned} равен {@code null},
 *       возвращаются все подборки; иначе — только закреплённые или незакреплённые.</li>
 * </ul>
 */
@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("""
            SELECT c FROM Compilation c
            WHERE :pinned IS NULL OR c.pinned = :pinned
            """)
    List<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);
}