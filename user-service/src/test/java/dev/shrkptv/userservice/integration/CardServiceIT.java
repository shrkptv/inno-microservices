package dev.shrkptv.userservice.integration;

import dev.shrkptv.userservice.database.entity.Card;
import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.dto.CardUpdateDTO;
import dev.shrkptv.userservice.exception.CardNotFoundException;
import dev.shrkptv.userservice.services.CardService;
import dev.shrkptv.userservice.services.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
@Testcontainers
public class CardServiceIT {

    @Autowired
    private CardService cardService;

    @Autowired
    private UserServiceImpl userService;

    @Test
    @DisplayName("Create and get card")
    void testCreateAndGetCard() {
        User user = new User();
        user.setName("Alexander");
        user.setSurname("Shirokopytov");
        user.setEmail("shrkptv@gmail.com");
        user.setBirthDate(LocalDate.of(2006, 6, 7));
        userService.createUser(user);

        Card card = new Card();
        card.setNumber("1234 5678 9012 3456");
        card.setExpirationDate(LocalDate.of(2030, 2, 8));
        card.setHolder("Belarusbank");

        Card createdCard = cardService.createCard(card, user.getId());
        assertNotNull(createdCard.getId());
        assertEquals("1234 5678 9012 3456", cardService.getCard(createdCard.getId()).getNumber());
    }

    @Test
    @DisplayName("Update card")
    void testUpdateCard() {
        User user = new User();
        user.setName("TestUser");
        user.setSurname("Test");
        user.setEmail("card.user@gmail.com");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        userService.createUser(user);

        Card card = new Card();
        card.setNumber("1111 1111 1111 1111");
        card.setExpirationDate(LocalDate.of(2027, 11,11));
        card.setHolder("Belarusbank");
        Card created = cardService.createCard(card, user.getId());

        CardUpdateDTO cardUpdateDTO = new CardUpdateDTO();
        cardUpdateDTO.setExpirationDate(LocalDate.of(2029, 11, 11));
        Card updatedCard = cardService.updateCard(created.getId(), cardUpdateDTO);

        assertEquals(LocalDate.of(2029, 11,11), updatedCard.getExpirationDate());
    }

    @Test
    @DisplayName("Delete card")
    void testDeleteCard() {
        User user = new User();
        user.setName("User");
        user.setSurname("Test");
        user.setEmail("user@gmail.com");
        user.setBirthDate(LocalDate.of(2002, 12, 1));
        userService.createUser(user);

        Card card = new Card();
        card.setNumber("2222 2222 2222 2222");
        card.setExpirationDate(LocalDate.of(2031, 1,1));
        card.setHolder("Belarusbank");
        Card createdCard = cardService.createCard(card, user.getId());

        Long id = createdCard.getId();
        cardService.deleteCard(id);

        assertThrows(CardNotFoundException.class, () -> cardService.getCard(id));
    }
}
