package dev.shrkptv.userservice.database.repository;

import dev.shrkptv.userservice.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT * FROM users WHERE id = :id", nativeQuery = true)
    Optional<User> findUserById(Long id);

    @Query(value = "SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findUserByEmail(String email);

    List<User> findUsersByIdIn(Collection<Long> ids);

    void deleteUserById(Long id);
}
