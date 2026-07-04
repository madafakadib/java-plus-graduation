package ru.practicum.user_service.mapper;

import ru.practicum.common.userDto.dto.NewUserRequest;
import ru.practicum.common.userDto.dto.UserDto;
import ru.practicum.common.userDto.dto.UserShortDto;
import ru.practicum.user_service.model.User;

public class UserMapper {
    public static User toUser(NewUserRequest request) {
        return User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static UserShortDto toUserShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
