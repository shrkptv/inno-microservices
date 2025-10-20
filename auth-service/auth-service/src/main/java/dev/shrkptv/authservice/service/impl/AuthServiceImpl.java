package dev.shrkptv.authservice.service.impl;

import dev.shrkptv.authservice.database.entity.AuthUser;
import dev.shrkptv.authservice.database.repository.AuthUserRepository;
import dev.shrkptv.authservice.dto.LoginRequestDTO;
import dev.shrkptv.authservice.dto.LoginResponseDTO;
import dev.shrkptv.authservice.dto.RegisterRequestDTO;
import dev.shrkptv.authservice.exception.InvalidTokenException;
import dev.shrkptv.authservice.security.JwtProvider;
import dev.shrkptv.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    private final AuthenticationConfiguration authenticationConfiguration;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException{
        return authUserRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User with login '" + login + "' not found"));
    }


    @Override
    public AuthUser save(RegisterRequestDTO registerRequestDTO) {
        if(authUserRepository.existsByLogin(registerRequestDTO.getLogin())){
            throw new UsernameNotFoundException(registerRequestDTO.getLogin());
        }

        AuthUser authUser = new AuthUser();
        authUser.setLogin(registerRequestDTO.getLogin());
        authUser.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        return authUserRepository.save(authUser);
    }

    @Override
    public LoginResponseDTO createAuthToken(LoginRequestDTO loginRequestDTO) {
        AuthenticationManager authenticationManager;
        try {
            authenticationManager = authenticationConfiguration.getAuthenticationManager();
        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain AuthenticationManager", e);
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDTO.getLogin(),
                        loginRequestDTO.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtProvider.generateAccessToken(loginRequestDTO.getLogin());
        String refreshToken = jwtProvider.generateRefreshToken(loginRequestDTO.getLogin());

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken(accessToken);
        loginResponseDTO.setRefreshToken(refreshToken);
        return loginResponseDTO;
    }

    @Override
    public LoginResponseDTO refreshAuthToken(String refreshToken) {
        if(!jwtProvider.validateRefreshToken(refreshToken)){
            throw new InvalidTokenException();
        }

        String login = jwtProvider.getLoginFromToken(refreshToken);

        String newAccessToken = jwtProvider.generateAccessToken(login);

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken(newAccessToken);
        loginResponseDTO.setRefreshToken(refreshToken);

        return loginResponseDTO;
    }

    @Override
    public boolean validateToken(String token) {
        if (jwtProvider.validateAccessToken(token) || jwtProvider.validateRefreshToken(token)) {;
            return true;
        } else {
            throw new InvalidTokenException();
        }
    }
}
