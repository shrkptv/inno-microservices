package dev.shrkptv.authservice.database.repository;

import dev.shrkptv.authservice.database.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByLogin(String login);

    boolean existsByLogin(String login);
}
