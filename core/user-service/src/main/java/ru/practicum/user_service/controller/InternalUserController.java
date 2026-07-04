package ru.practicum.user_service.controller.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.common.userDto.dto.UserShortDto;
import ru.practicum.user_service.service.InternalUserService;

@Slf4j
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final InternalUserService internalUserService;

    @GetMapping("/{userId}/exists")
    public boolean existsById(@PathVariable("userId") Long userId) {
        log.info("Internal API: Checking if user exists with id: {}", userId);
        return internalUserService.existsById(userId);
    }

    @GetMapping("/{userId}/short")
    public UserShortDto getUserShort(@PathVariable("userId") Long userId) {
        log.info("Internal API: Getting user short by id: {}", userId);
        return internalUserService.getUserShort(userId);
    }
}