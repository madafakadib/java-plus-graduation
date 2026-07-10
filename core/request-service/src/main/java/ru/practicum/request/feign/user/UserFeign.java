package ru.practicum.request.feign.user;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.UserShortDto;

import java.util.Map;
import java.util.Set;

@FeignClient(name = "user-service")
public interface UserFeign {
    @GetMapping("/admin/users/short/{userId}")
    UserShortDto findUserShortDtoById(@PathVariable("userId") Long userId);

    @PostMapping("/admin/users/short/map")
    Map<Long, UserShortDto> findUserShortDtoById(@RequestBody Set<Long> usersId);
}