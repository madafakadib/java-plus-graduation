package ru.practicum.compilation.service;


import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.Collection;

/**
 * Сервисный слой для работы с подборками событий.
 * <p>Инкапсулирует бизнес-логику административного и публичного API.</p>
 *
 * <ul>
 *   <li><b>Административные операции:</b>
 *       {@link #create(NewCompilationDto)},
 *       {@link #delete(Long)},
 *       {@link #update(Long, UpdateCompilationRequest)};</li>
 *   <li><b>Публичные операции:</b>
 *       {@link #getCompilations(Boolean, Integer, Integer)},
 *       {@link #getCompilationById(Long)}.</li>
 * </ul>
 *
 * <p>Все методы работают с DTO уровня API:
 * {@link ru.practicum.compilation.dto.CompilationDto},
 * {@link ru.practicum.compilation.dto.NewCompilationDto},
 * {@link ru.practicum.compilation.dto.UpdateCompilationRequest}.</p>
 */
public interface CompilationService {
    /**
     * Создаёт новую подборку событий.
     *
     * @param newCompilationDto входной DTO с данными новой подборки
     * @return созданная подборка
     */
    CompilationDto create(NewCompilationDto newCompilationDto);

    /**
     * Удаляет подборку по идентификатору.
     *
     * @param id идентификатор подборки
     */
    void delete(Long id);

    /**
     * Обновляет существующую подборку.
     *
     * @param id                       идентификатор подборки
     * @param updateCompilationRequest DTO с изменениями
     * @return обновлённая подборка
     */
    CompilationDto update(Long id, UpdateCompilationRequest updateCompilationRequest);

    /**
     * Возвращает подборки с фильтрацией по признаку закреплённости и пагинацией.
     *
     * @param pinned фильтр по закреплённости (nullable = все)
     * @param from   смещение (offset), ≥ 0
     * @param size   размер страницы, ≥ 1
     * @return коллекция подборок
     */
    Collection<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    /**
     * Возвращает подборку по идентификатору.
     *
     * @param compId идентификатор подборки
     * @return подборка
     */
    CompilationDto getCompilationById(Long compId);
}