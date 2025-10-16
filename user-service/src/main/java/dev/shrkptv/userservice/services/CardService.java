package dev.shrkptv.userservice.services;

import dev.shrkptv.userservice.dto.CardCreateDTO;
import dev.shrkptv.userservice.dto.CardResponseDTO;
import dev.shrkptv.userservice.dto.CardUpdateDTO;

import java.util.List;

public interface CardService {
    CardResponseDTO createCard(CardCreateDTO card, Long userId);
    CardResponseDTO getCard(Long id);
    List<CardResponseDTO> getCardList(List<Long> idList);
    CardResponseDTO updateCard(Long id, CardUpdateDTO cardUpdateDTO);
    void deleteCard(Long id);
}
