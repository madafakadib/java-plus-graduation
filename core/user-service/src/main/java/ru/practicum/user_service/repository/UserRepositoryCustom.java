package ru.practicum.user_service.repository;


import ru.practicum.common.userDto.dto.UserDto;

import java.util.List;

public interface UserRepositoryCustom {

    List<UserDto> findUsers(List<Long> ids, int from, int size);
}
