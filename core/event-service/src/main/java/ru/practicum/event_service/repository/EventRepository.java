package ru.practicum.event_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.event_service.model.Event;

import java.util.Collection;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryCustom {

    List<Event> findAllByIdIn(Collection<Long> eventIds);

    boolean existsByCategoryId(Long categoryId);

}
