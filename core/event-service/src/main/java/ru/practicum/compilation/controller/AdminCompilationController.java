package ru.practicum.compilation.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

/**
 * Админский REST-контроллер для управления подборками событий.
 * <p>Предоставляет CRUD-операции, доступные только администраторам.</p>
 *
 * <ul>
 *   <li>POST /admin/compilations — создание новой подборки;</li>
 *   <li>DELETE /admin/compilations/{compId} — удаление подборки;</li>
 *   <li>PATCH /admin/compilations/{compId} — обновление данных подборки.</li>
 * </ul>
 *
 * <p>Все запросы валидируются через {@link jakarta.validation.Valid}.
 * Бизнес-логика реализована в {@link ru.practicum.compilation.service.CompilationService}.</p>
 */
@RestController
@RequestMapping(path = "/admin/compilations")
@RequiredArgsConstructor
@Validated
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto create(@Valid @RequestBody NewCompilationDto newCompilationDto) {

        return compilationService.create(newCompilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("compId") long id) {
        compilationService.delete(id);
    }

    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable("compId") long id,
                                 @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        return compilationService.update(id, updateCompilationRequest);
    }
}