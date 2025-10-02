package dev.shrkptv.userservice.services.impl;

import dev.shrkptv.userservice.database.entity.Card;
import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.database.repository.CardRepository;
import dev.shrkptv.userservice.database.repository.UserRepository;
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
    public Card createCard(Card card, Long userId) {
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UserNotFoundByIdException(userId));
        card.setUser(user);
        return cardRepository.save(card);
    }

    @Override
    @Cacheable(value = "cards", key = "#id")
    public Card getCard(Long id) {
        return cardRepository.findCardById(id).orElseThrow(() -> new CardNotFoundException(id));
    }

    public List<Card> getCardList(List<Long> idList) {
        return cardRepository.findCardsByIdIn(idList);
    }

    @Override
    @Transactional
    @CachePut(value = "cards", key = "#id")
    public Card updateCard(Long id, CardUpdateDTO cardUpdateDTO) {
        Card card = cardRepository.findCardById(id).orElseThrow(() -> new CardNotFoundException(id));
        cardMapper.updateEntityFromDto(cardUpdateDTO, card);
        return card;
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
