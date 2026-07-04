package ru.practicum.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.common.exceptions.exceptions.NotFoundException;
import ru.practicum.common.userDto.dto.UserShortDto;
import ru.practicum.user_service.mapper.UserMapper;
import ru.practicum.user_service.model.User;
import ru.practicum.user_service.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternalUserService {

    private final UserRepository userRepository;

    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    public UserShortDto getUserShort(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        return UserMapper.toUserShortDto(user);
    }
}