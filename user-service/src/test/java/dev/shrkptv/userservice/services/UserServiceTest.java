package dev.shrkptv.userservice.services;

import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.database.repository.UserRepository;
import dev.shrkptv.userservice.dto.UserUpdateDTO;
import dev.shrkptv.userservice.exception.UserAlreadyExistsException;
import dev.shrkptv.userservice.exception.UserNotFoundByEmailException;
import dev.shrkptv.userservice.exception.UserNotFoundByIdException;
import dev.shrkptv.userservice.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Create new user when email is not taken")
    void testCreateUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        when(userRepository.findUserByEmail(user.getEmail()))
                .thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertEquals(user.getId(), result.getId());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("Throw exception when creating user with existing email")
    void testCreateUserAlreadyExists() {
        User user = new User();
        user.setEmail("test@gmail.com");

        when(userRepository.findUserByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> userService.createUser(user));
    }

    @Test
    @DisplayName("Return user by id")
    void testGetUserById() {
        User user = new User();
        user.setId(2L);

        when(userRepository.findUserById(2L))
                .thenReturn(Optional.of(user));

        assertEquals(user.getId(), userService.getUserById(2L).getId());
    }

    @Test
    @DisplayName("Throw exception when user not found by id")
    void testGetUserByIdNotFound() {
        when(userRepository.findUserById(2L))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundByIdException.class, () -> userService.getUserById(2L));
    }

    @Test
    @DisplayName("Return user by email")
    void testGetUserByEmail() {
        User user = new User();
        user.setEmail("test@gmail.com");

        when(userRepository.findUserByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        assertEquals(user.getEmail(), userService.getUserByEmail("test@gmail.com").getEmail());
    }

    @Test
    @DisplayName("Throw exception when user not found by email")
    void testGetUserByEmailNotFound() {
        when(userRepository.findUserByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundByEmailException.class, () -> userService.getUserByEmail("test@gmail.com"));
    }

    @Test
    @DisplayName("Return list of users by id list")
    void testGetUserList() {
        User firstUser = new User();
        firstUser.setId(1L);

        User secondUser = new User();
        secondUser.setId(2L);

        when(userRepository.findUsersByIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(firstUser, secondUser));

        assertEquals(2, userService.getUserList(List.of(1L, 2L)).size());
    }

    @Test
    @DisplayName("Return empty list when id list has no users")
    void testGetUserListEmpty() {
        when(userRepository.findUsersByIdIn(List.of(1L, 2L))).thenReturn(List.of());

        assertTrue(userService.getUserList(List.of(1L, 2L)).isEmpty());
    }

    @Test
    @DisplayName("Update existing user")
    void testUpdateUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("previous@gmail.com");

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setEmail("new@gmail.com");

        when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findUserByEmail("new@gmail.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> userService.updateUser(1L, userUpdateDTO));
    }

    @Test
    @DisplayName("Throw exception when updating user not found by id")
    void testUpdateUserNotFound(){
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setEmail("test@gmail.com");

        when(userRepository.findUserById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundByIdException.class, () -> userService.updateUser(1L, userUpdateDTO));
    }

    @Test
    @DisplayName("Throw exception when updating user with existing email")
    void testUpdateUserEmailAlreadyExists(){
        User user = new User();
        user.setId(1L);
        user.setEmail("previous@gmail.com");

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setEmail("new@gmail.com");

        User existingUser = new User();
        existingUser.setEmail("new@gmail.com");

        when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findUserByEmail("new@gmail.com")).thenReturn(Optional.of(existingUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(1L, userUpdateDTO));
    }

    @Test
    @DisplayName("Delete user without exception")
    void testDeleteUser() {
        when(userRepository.existsById(2L)).thenReturn(true);
        doNothing().when(userRepository).deleteUserById(2L);

        assertDoesNotThrow(() -> userService.deleteUser(2L));
    }

    @Test
    @DisplayName("Throw exception when deleting user not found by id")
    void testDeleteUserNotFound() {
        when(userRepository.existsById(2L)).thenReturn(false);

        assertThrows(UserNotFoundByIdException.class, () -> userService.deleteUser(2L));
    }
}