package dev.shrkptv.userservice.services;

import dev.shrkptv.userservice.database.entity.Card;
import dev.shrkptv.userservice.dto.CardUpdateDTO;

import java.util.List;

public interface CardService {
    Card createCard(Card card, Long userId);
    Card getCard(Long id);
    List<Card> getCardList(List<Long> idList);
    Card updateCard(Long id, CardUpdateDTO cardUpdateDTO);
    void deleteCard(Long id);
}
