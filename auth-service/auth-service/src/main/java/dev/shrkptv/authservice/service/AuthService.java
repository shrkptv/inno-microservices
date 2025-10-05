package dev.shrkptv.authservice.service;

import dev.shrkptv.authservice.database.entity.AuthUser;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {
    boolean existsByLogin(String login);

    AuthUser save(AuthUser authUser);
}
