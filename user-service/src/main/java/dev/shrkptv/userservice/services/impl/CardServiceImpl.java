package dev.shrkptv.userservice.services.impl;

import dev.shrkptv.userservice.database.entity.Card;
import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.database.repository.CardRepository;
import dev.shrkptv.userservice.database.repository.UserRepository;
import dev.shrkptv.userservice.dto.CardCreateDTO;
import dev.shrkptv.userservice.dto.CardResponseDTO;
import dev.shrkptv.userservice.dto.CardUpdateDTO;
import dev.shrkptv.userservice.exception.CardNotFoundException;
import dev.shrkptv.userservice.exception.UserNotFoundByIdException;
import dev.shrkptv.userservice.mapper.CardMapper;
import dev.shrkptv.userservice.services.CardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    @Override
    @CachePut(value = "cards", key = "#result.id")
    public CardResponseDTO createCard(CardCreateDTO cardCreateDTO, Long userId) {
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UserNotFoundByIdException(userId));
        Card card = cardMapper.toEntity(cardCreateDTO);
        card.setUser(user);
        cardRepository.save(card);
        return cardMapper.toDto(card);
    }

    @Override
    @Cacheable(value = "cards", key = "#id")
    public CardResponseDTO getCard(Long id) {
        Card card = cardRepository.findCardById(id).orElseThrow(() -> new CardNotFoundException(id));
        return cardMapper.toDto(card);
    }

    public List<CardResponseDTO> getCardList(List<Long> idList) {
        return cardRepository.findCardsByIdIn(idList)
                .stream()
                .map(cardMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    @CachePut(value = "cards", key = "#id")
    public CardResponseDTO updateCard(Long id, CardUpdateDTO cardUpdateDTO) {
        Card card = cardRepository.findCardById(id).orElseThrow(() -> new CardNotFoundException(id));
        cardMapper.updateEntityFromDto(cardUpdateDTO, card);
        return cardMapper.toDto(card);
    }

    @Override
    @Transactional
    @CacheEvict(value = "cards", key = "#id")
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new CardNotFoundException(id);
        }

        cardRepository.deleteCardById(id);
    }
}
