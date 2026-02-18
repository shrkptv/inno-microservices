package dev.shrkptv.authservice.service.impl;

import dev.shrkptv.authservice.client.UserFeignClient;
import dev.shrkptv.authservice.database.entity.AuthUser;
import dev.shrkptv.authservice.database.repository.AuthUserRepository;
import dev.shrkptv.authservice.dto.LoginRequestDTO;
import dev.shrkptv.authservice.dto.LoginResponseDTO;
import dev.shrkptv.authservice.dto.RegisterRequestDTO;
import dev.shrkptv.authservice.dto.UserCreateRequestDTO;
import dev.shrkptv.authservice.exception.FailedRegistrationException;
import dev.shrkptv.authservice.exception.InvalidTokenException;
import dev.shrkptv.authservice.security.JwtProvider;
import dev.shrkptv.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthUserRepository authUserRepository;
    private final JwtProvider jwtProvider;
    private final UserFeignClient userFeignClient;
    private final Keycloak keycloak;
    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtDecoder jwtDecoder;

    @Value("${kc.server-url}")
    private String serverUrl;
    @Value("${kc.realm}")
    private String realm;
    @Value("${kc.client-id}")
    private String clientId;
    @Value("${kc.client-secret}")
    private String clientSecret;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException{
        return authUserRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User with login '" + login + "' not found"));
    }


    @Override
    public AuthUser save(RegisterRequestDTO registerRequestDTO) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(registerRequestDTO.getLogin());
        user.setEmail(registerRequestDTO.getLogin());
        user.setFirstName(registerRequestDTO.getName());
        user.setLastName(registerRequestDTO.getSurname());

        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(registerRequestDTO.getPassword());
        user.setCredentials(Collections.singletonList(passwordCred));

        try {
            var response = keycloak.realm(realm).users().create(user);

            if (response.getStatus() != 201) {
                log.error("Keycloak registration failed. Status: {}", response.getStatus());
                throw new FailedRegistrationException();
            }

            UserCreateRequestDTO userCreateRequestDTO = new UserCreateRequestDTO();
            userCreateRequestDTO.setName(registerRequestDTO.getName());
            userCreateRequestDTO.setSurname(registerRequestDTO.getSurname());
            userCreateRequestDTO.setBirthDate(registerRequestDTO.getBirthDate());
            userCreateRequestDTO.setEmail(registerRequestDTO.getLogin());

            userFeignClient.createUser(userCreateRequestDTO);

            AuthUser authUser = new AuthUser();
            authUser.setLogin(registerRequestDTO.getLogin());
            return authUser;

        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            throw new FailedRegistrationException();
        }
    }

    @Override
    public LoginResponseDTO createAuthToken(LoginRequestDTO loginRequestDTO) {
        String url = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("username", loginRequestDTO.getLogin());
        map.add("password", loginRequestDTO.getPassword());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> responseBody = response.getBody();

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken((String) responseBody.get("access_token"));
        loginResponseDTO.setRefreshToken((String) responseBody.get("refresh_token"));

        return loginResponseDTO;
    }

    @Override
    public LoginResponseDTO refreshAuthToken(String refreshToken) {
        String url = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "refresh_token");
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
            loginResponseDTO.setAccessToken((String) responseBody.get("access_token"));
            loginResponseDTO.setRefreshToken((String) responseBody.get("refresh_token"));
            return loginResponseDTO;
        } catch (Exception e) {
            throw new InvalidTokenException();
        }
    }

    public String validateToken(String authHeader) {
        String tokenValue = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        try {
            Jwt jwt = jwtDecoder.decode(tokenValue);

            String username = jwt.getClaimAsString("preferred_username");
            if (username == null) {
                username = jwt.getSubject();
            }
            return username;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new InvalidTokenException();
        }
    }
}
