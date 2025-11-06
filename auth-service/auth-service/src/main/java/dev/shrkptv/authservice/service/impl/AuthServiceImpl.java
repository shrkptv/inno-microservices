package dev.shrkptv.authservice.service.impl;

import dev.shrkptv.authservice.client.UserFeignClient;
import dev.shrkptv.authservice.database.entity.AuthUser;
import dev.shrkptv.authservice.database.repository.AuthUserRepository;
import dev.shrkptv.authservice.dto.LoginRequestDTO;
import dev.shrkptv.authservice.dto.LoginResponseDTO;
import dev.shrkptv.authservice.dto.RegisterRequestDTO;
import dev.shrkptv.authservice.dto.UserCreateRequestDTO;
import dev.shrkptv.authservice.dto.UserResponseDTO;
import dev.shrkptv.authservice.exception.FailedRegistrationException;
import dev.shrkptv.authservice.exception.InvalidTokenException;
import dev.shrkptv.authservice.security.JwtProvider;
import dev.shrkptv.authservice.service.AuthService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserFeignClient userFeignClient;
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

        UserCreateRequestDTO userCreateRequestDTO = new UserCreateRequestDTO();
        userCreateRequestDTO.setName(registerRequestDTO.getName());
        userCreateRequestDTO.setSurname(registerRequestDTO.getSurname());
        userCreateRequestDTO.setBirthDate(registerRequestDTO.getBirthDate());
        userCreateRequestDTO.setEmail(registerRequestDTO.getLogin());

        AuthUser authUser = new AuthUser();
        authUser.setLogin(registerRequestDTO.getLogin());
        authUser.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));

        Long userId = null;

        try {
            UserResponseDTO createdUser = userFeignClient.createUser(userCreateRequestDTO).getBody();
            userId = createdUser.getId();
            return authUserRepository.save(authUser);
        }
        catch (FeignException e){
            log.error("FeignException while creating user in user-service: {}", e.getMessage(), e);
            throw new FailedRegistrationException();
        }
        catch (Exception e){
            log.error("Exception while creating auth user in auth-service: {}", e.getMessage(), e);
            try {
                if(userId != null){
                    userFeignClient.deleteUser(userId);
                }
            } catch (FeignException fe) {
                log.error("FeignException during delete in user-service: {}", fe.getMessage(), fe);
            }
            throw new FailedRegistrationException();
        }
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
