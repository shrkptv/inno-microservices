package dev.shrkptv.userservice.services;

import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(Long id);
    User getUserByEmail(String email);
    List<User> getUserList(List<Long> idList);
    User updateUser(Long id, UserUpdateDTO userUpdateDTO);
    void deleteUser(Long id);
}
