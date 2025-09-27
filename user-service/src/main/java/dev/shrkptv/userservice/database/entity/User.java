package dev.shrkptv.userservice.database.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User extends BaseEntity<Long>{

    private String name;
    private String surname;

    @Column(name="birth_date")
    private LocalDate birthDate;
    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Card> cards;

    public void addCard(Card card) {
        cards.add(card);
        card.setUser(this);
    }

    public void removeCard(Card card) {
        cards.remove(card);
        card.setUser(null);
    }

}
