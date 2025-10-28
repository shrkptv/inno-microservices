package dev.shrkptv.userservice.integration;

import dev.shrkptv.userservice.dto.UserCreateDTO;
import dev.shrkptv.userservice.dto.UserResponseDTO;
import dev.shrkptv.userservice.dto.UserUpdateDTO;
import dev.shrkptv.userservice.exception.UserNotFoundByIdException;
import dev.shrkptv.userservice.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceIT extends AbstractIT{

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Create and get user")
    void testCreateAndGetUser(){
        UserResponseDTO createdUser = createTestUser("Danial", "Ivanov", "test@gmail.com",
                LocalDate.of(1999, 12, 16));

        assertNotNull(createdUser.getId());
        assertEquals("Danial", userService.getUserById(createdUser.getId()).getName());
    }

    @Test
    @DisplayName("Update user")
    void testUpdateUser(){
        UserResponseDTO createdUser = createTestUser("Oksana", "Radionova", "oks@gmail.com",
                LocalDate.of(2000, 1, 1));

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setSurname("Budko");

        UserResponseDTO updatedUser = userService.updateUser(createdUser.getId(), userUpdateDTO);

        assertEquals("Budko", updatedUser.getSurname());
    }

    @Test
    @DisplayName("Delete user")
    void testDeleteUser(){
        UserResponseDTO createdUser = createTestUser("Ivan", "Ivanov", "iva@gmail.com",
                LocalDate.of(1999, 5,5));
        Long id = createdUser.getId();
        userService.deleteUser(id);

        assertThrows(UserNotFoundByIdException.class, () -> userService.getUserById(id));
    }

    private UserResponseDTO createTestUser(String name, String surname, String email, LocalDate birthDate) {
        UserCreateDTO user = new UserCreateDTO();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setBirthDate(birthDate);
        userService.createUser(user);
        return userService.getUserByEmail(email);
    }
}
