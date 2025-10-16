package dev.shrkptv.userservice.services.impl;

import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.database.repository.UserRepository;
import dev.shrkptv.userservice.dto.UserCreateDTO;
import dev.shrkptv.userservice.dto.UserResponseDTO;
import dev.shrkptv.userservice.dto.UserUpdateDTO;
import dev.shrkptv.userservice.exception.UserAlreadyExistsException;
import dev.shrkptv.userservice.exception.UserNotFoundByEmailException;
import dev.shrkptv.userservice.exception.UserNotFoundByIdException;
import dev.shrkptv.userservice.mapper.UserMapper;
import dev.shrkptv.userservice.services.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @CachePut(value = "users", key = "#result.id")
    public UserResponseDTO createUser(UserCreateDTO userCreateDTO) {
        User user = userMapper.toEntity(userCreateDTO);
        if (userRepository.findUserByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(user.getEmail());
        }
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findUserById(id).orElseThrow(() -> new UserNotFoundByIdException(id));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundByEmailException(email));
        return userMapper.toDto(user);
    }


    @Override
    public List<UserResponseDTO> getUserList(List<Long> idList) {
        return userRepository.findUsersByIdIn(idList)
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findUserById(id).orElseThrow(() -> new UserNotFoundByIdException(id));
        if (userUpdateDTO.getEmail() != null
                && !userUpdateDTO.getEmail().equals(user.getEmail())) {
            userRepository.findUserByEmail(userUpdateDTO.getEmail())
                    .ifPresent(existing -> {
                        throw new UserAlreadyExistsException(userUpdateDTO.getEmail());
                    });
        }
        userMapper.updateEntityFromDto(userUpdateDTO, user);
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundByIdException(id);
        }

        userRepository.deleteUserById(id);
    }
}
