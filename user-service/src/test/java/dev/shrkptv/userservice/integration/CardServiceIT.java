package dev.shrkptv.userservice.integration;

import dev.shrkptv.userservice.dto.CardCreateDTO;
import dev.shrkptv.userservice.dto.CardResponseDTO;
import dev.shrkptv.userservice.dto.CardUpdateDTO;
import dev.shrkptv.userservice.dto.UserCreateDTO;
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
        Long userId = createTestUser("Alexander", "Shirokopytov", "shrkptv@gmail.com",
                LocalDate.of(2006, 6, 7));

        CardResponseDTO createdCard = createTestCard(userId, "1234 5678 9012 3456",
                LocalDate.of(2030, 2, 8), "Belarusbank");
        assertNotNull(createdCard.getId());
        assertEquals("1234 5678 9012 3456", cardService.getCard(createdCard.getId()).getNumber());
    }

    @Test
    @DisplayName("Update card")
    void testUpdateCard() {
        Long userId = createTestUser("TestUser", "Test", "card_user@gmail.com",
                LocalDate.of(2000, 1, 1));

        CardResponseDTO created = createTestCard(userId, "1111 1111 1111 1111",
                LocalDate.of(2027, 11,11), "Belarusbank");

        CardUpdateDTO cardUpdateDTO = new CardUpdateDTO();
        cardUpdateDTO.setExpirationDate(LocalDate.of(2029, 11, 11));
        CardResponseDTO updatedCard = cardService.updateCard(created.getId(), cardUpdateDTO);

        assertEquals(LocalDate.of(2029, 11,11), updatedCard.getExpirationDate());
    }

    @Test
    @DisplayName("Delete card")
    void testDeleteCard() {
        Long userId = createTestUser("User", "Test", "user@gmail.com",
                LocalDate.of(2002, 12, 1));

        CardResponseDTO createdCard = createTestCard(userId, "2222 2222 2222 2222",
                LocalDate.of(2031, 1,1), "Belarusbank");

        Long id = createdCard.getId();
        cardService.deleteCard(id);

        assertThrows(CardNotFoundException.class, () -> cardService.getCard(id));
    }

    private Long createTestUser(String name, String surname, String email, LocalDate birthDate) {
        UserCreateDTO user = new UserCreateDTO();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setBirthDate(birthDate);
        userService.createUser(user);
        return userService.getUserByEmail(email).getId();
    }

    private CardResponseDTO createTestCard(Long userId, String number, LocalDate expirationDate, String holder) {
        CardCreateDTO card = new CardCreateDTO();
        card.setNumber(number);
        card.setExpirationDate(expirationDate);
        card.setHolder(holder);
        return cardService.createCard(card, userId);
    }
}
