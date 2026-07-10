package ru.practicum.category.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.category.model.Category;
/**
 * Spring Data JPA-репозиторий для сущностей {@link Category}.
 * <p>Предоставляет стандартные CRUD-операции через {@link JpaRepository}.</p>
 *
 * <p>Специфичной логики не содержит; используется в {@code CategoryService}.</p>
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}