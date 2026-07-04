package ru.practicum.user_service.service;


import ru.practicum.common.userDto.dto.NewUserRequest;
import ru.practicum.common.userDto.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest newUserRequest);

    List<UserDto> findUsers(List<Long> ids, int from, int size);

    void deleteUser(Long userId);
}
