package dev.shrkptv.authservice.controller;

import dev.shrkptv.authservice.database.entity.AuthUser;
import dev.shrkptv.authservice.database.repository.AuthUserRepository;
import dev.shrkptv.authservice.dto.LoginRequestDTO;
import dev.shrkptv.authservice.dto.LoginResponseDTO;
import dev.shrkptv.authservice.dto.RegisterRequestDTO;
import dev.shrkptv.authservice.security.JwtProvider;
import dev.shrkptv.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) throws Exception {
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
        return ResponseEntity.ok(loginResponseDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO registerRequestDTO) throws Exception {
        if(authService.existsByLogin(registerRequestDTO.getLogin())){
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        AuthUser authUser = new AuthUser();
        authUser.setLogin(registerRequestDTO.getLogin());
        authUser.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));

        authService.save(authUser);

        return ResponseEntity.ok("Successfully registered!");
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestParam String refreshToken) {
        if(!jwtProvider.validateRefreshToken(refreshToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String login = jwtProvider.getLoginFromToken(refreshToken);

        String newAccessToken = jwtProvider.generateAccessToken(login);

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken(newAccessToken);
        loginResponseDTO.setRefreshToken(refreshToken);

        return ResponseEntity.ok(loginResponseDTO);
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestParam String token) {
        boolean valid = jwtProvider.validateAccessToken(token) || jwtProvider.validateRefreshToken(token);
        return valid ? ResponseEntity.ok("Token is valid!")
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is not valid!");
    }
}
