package dev.shrkptv.userservice.services;

import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.database.repository.UserRepository;
import dev.shrkptv.userservice.dto.UserUpdateDTO;
import dev.shrkptv.userservice.mapper.UserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUser(Long id) {
        return userRepository.findUserById(id);
    }

    public List<User> getUserList(List<Long> idList) {
        return userRepository.findUsersByIdIn(idList);
    }

    @Transactional
    public Optional<User> updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        Optional<User> user = userRepository.findUserById(id);
        user.ifPresent(u -> userMapper.updateEntityFromDto(userUpdateDTO, u));
        return user;
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteUserById(id);
    }
}
