package ru.practicum.compilation_service.service;



import ru.practicum.common.compilationDto.dto.CompilationDto;
import ru.practicum.common.compilationDto.dto.NewCompilationDto;
import ru.practicum.common.compilationDto.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto postCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto patchCompilation(Long compId, UpdateCompilationRequest updateRequest);

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(Long compId);
}
