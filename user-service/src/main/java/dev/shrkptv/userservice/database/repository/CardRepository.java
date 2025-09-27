package dev.shrkptv.userservice.database.repository;

import dev.shrkptv.userservice.database.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query(value = "SELECT c FROM Card c WHERE c.id = :id")
    Optional<Card> findCardById(Long id);

    List<Card> findCardsByIdIn(Collection<Long> ids);

    void deleteCardById(Long id);
}
