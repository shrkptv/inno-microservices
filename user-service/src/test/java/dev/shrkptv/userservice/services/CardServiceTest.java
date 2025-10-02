package dev.shrkptv.userservice.services;

import dev.shrkptv.userservice.database.entity.Card;
import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.database.repository.CardRepository;
import dev.shrkptv.userservice.database.repository.UserRepository;
import dev.shrkptv.userservice.dto.CardUpdateDTO;
import dev.shrkptv.userservice.exception.CardNotFoundException;
import dev.shrkptv.userservice.exception.UserNotFoundByIdException;
import dev.shrkptv.userservice.mapper.CardMapper;
import dev.shrkptv.userservice.services.CardService;
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
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardMapper cardMapper;
    @InjectMocks
    private CardService cardService;

    @Test
    @DisplayName("Create card for existing user")
    void testCreateCard() {
        User user = new User();
        user.setId(1L);

        Card card = new Card();
        card.setId(9L);

        when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.save(card)).thenReturn(card);

        assertEquals(card.getId(), cardService.createCard(card, 1L).getId());
    }

    @Test
    @DisplayName("Throw exception when creating card for non-existing user")
    void testCreateCardUserNotFound(){
        Card card = new Card();
        when(userRepository.findUserById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundByIdException.class,
                () -> cardService.createCard(card, 1L));
    }

    @Test
    @DisplayName("Return card by id")
    void testGetCard() {
        Card card = new Card();
        card.setId(10L);

        when(cardRepository.findCardById(10L)).thenReturn(Optional.of(card));

        assertEquals(card.getId(), cardService.getCard(10L).getId());
    }

    @Test
    @DisplayName("Throw exception when card not found by id")
    void testGetCardNotFound(){
        when(cardRepository.findCardById(10L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCard(10L));
    }

    @Test
    @DisplayName("Return list of cards by id list")
    void testGetCardList() {
        Card firstCard = new Card();
        firstCard.setId(1L);

        Card secondCard = new Card();
        secondCard.setId(2L);

        when(cardRepository.findCardsByIdIn(List.of(1L, 2L))).thenReturn(List.of(firstCard, secondCard));

        assertEquals(2, cardService.getCardList(List.of(1L, 2L)).size());
    }

    @Test
    @DisplayName("Return empty list when id list has no cards")
    void testGetCardListEmpty(){
        when(cardRepository.findCardsByIdIn(List.of(1L, 2L))).thenReturn(List.of());

        assertTrue(cardService.getCardList(List.of(1L, 2L)).isEmpty());
    }

    @Test
    @DisplayName("Update existing card")
    void testUpdateCard() {
        Card card = new Card();
        card.setId(1L);

        CardUpdateDTO cardUpdateDTO = new CardUpdateDTO();

        when(cardRepository.findCardById(1L)).thenReturn(Optional.of(card));

        assertDoesNotThrow(() -> cardService.updateCard(1L, cardUpdateDTO));
    }

    @Test
    @DisplayName("Throw exception when updating card not found by id")
    void testUpdateCardNotFound(){
        CardUpdateDTO cardUpdateDTO = new CardUpdateDTO();
        when(cardRepository.findCardById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.updateCard(1L, cardUpdateDTO));
    }

    @Test
    @DisplayName("Delete card without exception")
    void testDeleteCard() {
        when(cardRepository.existsById(2L)).thenReturn(true);
        doNothing().when(cardRepository).deleteCardById(2L);

        assertDoesNotThrow(() -> cardService.deleteCard(2L));
    }

    @Test
    @DisplayName("Throw exception when deleting card not found by id")
    void testDeleteCardNotFound(){
        when(cardRepository.existsById(2L)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(2L));
    }
}