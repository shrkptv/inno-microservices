package dev.shrkptv.userservice.services;

import dev.shrkptv.userservice.database.entity.Card;
import dev.shrkptv.userservice.database.entity.User;
import dev.shrkptv.userservice.database.repository.CardRepository;
import dev.shrkptv.userservice.database.repository.UserRepository;
import dev.shrkptv.userservice.dto.CardUpdateDTO;
import dev.shrkptv.userservice.dto.UserUpdateDTO;
import dev.shrkptv.userservice.mapper.CardMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;

    public Card createCard(Card card, Long userId) {
        Optional<User> user = userRepository.findUserById(userId);
        user.ifPresent(card::setUser);
        return cardRepository.save(card);
    }

    public Optional<Card> getUser(Long id) {
        return cardRepository.findCardById(id);
    }

    public List<Card> getCardList(List<Long> idList) {
        return cardRepository.findCardsByIdIn(idList);
    }

    @Transactional
    public Optional<Card> updateCard(Long id, CardUpdateDTO cardUpdateDTO) {
        Optional<Card> card = cardRepository.findCardById(id);
        card.ifPresent(c -> cardMapper.updateEntityFromDto(cardUpdateDTO, c));
        return card;
    }

    @Transactional
    public void deleteCard(Long id) {
        cardRepository.deleteCardById(id);
    }
}
