package ru.practicum.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.common.userDto.dto.UserShortDto;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/{userId}")
    boolean existsById(@PathVariable("userId") Long userId);

    @GetMapping("/{userId}/short")
    UserShortDto getUserShort(@PathVariable("userId") Long userId);
}
