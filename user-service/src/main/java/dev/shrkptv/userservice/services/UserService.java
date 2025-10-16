package dev.shrkptv.userservice.services;

import dev.shrkptv.userservice.dto.UserCreateDTO;
import dev.shrkptv.userservice.dto.UserResponseDTO;
import dev.shrkptv.userservice.dto.UserUpdateDTO;

import java.util.List;

public interface UserService {
    UserResponseDTO createUser(UserCreateDTO userCreateDTO);
    UserResponseDTO getUserById(Long id);
    UserResponseDTO getUserByEmail(String email);
    List<UserResponseDTO> getUserList(List<Long> idList);
    UserResponseDTO updateUser(Long id, UserUpdateDTO userUpdateDTO);
    void deleteUser(Long id);
}
