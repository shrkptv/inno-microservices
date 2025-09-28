package dev.shrkptv.userservice.integration;

import dev.shrkptv.userservice.UserServiceApplication;
import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.dto.UserUpdateDTO;
import dev.shrkptv.userservice.exception.UserNotFoundByIdException;
import dev.shrkptv.userservice.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
public class UserServiceIT {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Create and get user")
    void testCreateAndGetUser(){
        User user = new User();
        user.setName("Danial");
        user.setSurname("Ivanov");
        user.setEmail("test@gmail.com");
        user.setBirthDate(LocalDate.of(1999, 12, 16));

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser.getId());
        assertEquals("Danial", userService.getUserById(createdUser.getId()).getName());
    }

    @Test
    @DisplayName("Update user")
    void testUpdateUser(){
        User user = new User();
        user.setName("Oksana");
        user.setSurname("Radionova");
        user.setEmail("oks@gmail.com");
        user.setBirthDate(LocalDate.of(2000, 1, 1));

        User createdUser = userService.createUser(user);

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setSurname("Budko");

        User updatedUser = userService.updateUser(createdUser.getId(), userUpdateDTO);

        assertEquals("Budko", updatedUser.getSurname());
    }

    @Test
    @DisplayName("Delete user")
    void testDeleteUser(){
        User user = new User();
        user.setName("Ivan");
        user.setSurname("Ivanov");
        user.setEmail("iva@gmail.com");
        user.setBirthDate(LocalDate.of(1999, 5,5));

        User createdUser = userService.createUser(user);
        Long id = createdUser.getId();
        userService.deleteUser(id);

        assertThrows(UserNotFoundByIdException.class, () -> userService.getUserById(id));
    }
}
