package dev.shrkptv.userservice.services;

import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.database.repository.UserRepository;
import dev.shrkptv.userservice.dto.UserUpdateDTO;
import dev.shrkptv.userservice.exception.UserAlreadyExistsException;
import dev.shrkptv.userservice.exception.UserNotFoundByEmailException;
import dev.shrkptv.userservice.exception.UserNotFoundByIdException;
import dev.shrkptv.userservice.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @CachePut(value = "users", key = "#result.id")
    public User createUser(User user) {
        userRepository.findUserByEmail(user.getEmail())
                .ifPresent(existing -> {
                    throw new UserAlreadyExistsException(user.getEmail());
                });
        return userRepository.save(user);
    }

    @Cacheable(value = "users", key = "#id")
    public User getUserById(Long id) {
        return userRepository.findUserById(id).orElseThrow(() -> new UserNotFoundByIdException(id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundByEmailException(email));
    }


    public List<User> getUserList(List<Long> idList) {
        return userRepository.findUsersByIdIn(idList);
    }

    @Transactional
    @CachePut(value = "users", key = "#id")
    public User updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findUserById(id).orElseThrow(() -> new UserNotFoundByIdException(id));
        if (userUpdateDTO.getEmail() != null
                && !userUpdateDTO.getEmail().equals(user.getEmail())) {
            userRepository.findUserByEmail(userUpdateDTO.getEmail())
                    .orElseThrow(() -> new UserAlreadyExistsException(userUpdateDTO.getEmail()));
        }
        userMapper.updateEntityFromDto(userUpdateDTO, user);
        return user;
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        userRepository.deleteUserById(id);
    }
}
