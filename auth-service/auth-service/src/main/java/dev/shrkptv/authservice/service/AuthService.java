package dev.shrkptv.authservice.service;

import dev.shrkptv.authservice.database.entity.AuthUser;
import dev.shrkptv.authservice.dto.LoginRequestDTO;
import dev.shrkptv.authservice.dto.LoginResponseDTO;
import dev.shrkptv.authservice.dto.RegisterRequestDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthService extends UserDetailsService {
    AuthUser save(RegisterRequestDTO registerRequestDTO);

    LoginResponseDTO createAuthToken(LoginRequestDTO loginRequestDTO);

    LoginResponseDTO refreshAuthToken(String refreshToken);

    boolean validateToken(String token);
}
