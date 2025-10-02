package dev.shrkptv.userservice.controller;

import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.dto.UserCreateDTO;
import dev.shrkptv.userservice.dto.UserResponseDTO;
import dev.shrkptv.userservice.dto.UserUpdateDTO;
import dev.shrkptv.userservice.mapper.UserMapper;
import dev.shrkptv.userservice.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO)
    {
        User user = userService.createUser(userMapper.toEntity(userCreateDTO));
        return ResponseEntity.status(201).body(userMapper.toDto(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserBy(@PathVariable Long id)
    {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getUserList(@RequestParam List<Long> idList)
    {
        List<User> userList = userService.getUserList(idList);
        return ResponseEntity.ok(userMapper.toDtoList(userList));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO)
    {
        User updatedUser = userService.updateUser(id, userUpdateDTO);
        return ResponseEntity.ok(userMapper.toDto(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id)
    {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
