package dev.shrkptv.userservice.repository;

import dev.shrkptv.userservice.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    @Query(value = "SELECT c FROM Card c WHERE c.id = :id")
    Card findCardById(Long id);

    @Query(value = "SELECT * FROM card_info WHERE id IN(:ids)", nativeQuery = true)
    List<Card> findCardsByIdIn(Collection<Long> ids);

    void deleteCardById(Long id);

}
