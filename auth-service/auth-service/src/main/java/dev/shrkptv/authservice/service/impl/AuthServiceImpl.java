package dev.shrkptv.authservice.service.impl;

import dev.shrkptv.authservice.client.UserFeignClient;
import dev.shrkptv.authservice.dto.LoginRequestDTO;
import dev.shrkptv.authservice.dto.LoginResponseDTO;
import dev.shrkptv.authservice.dto.RegisterRequestDTO;
import dev.shrkptv.authservice.dto.UserCreateRequestDTO;
import dev.shrkptv.authservice.exception.FailedRegistrationException;
import dev.shrkptv.authservice.exception.InvalidTokenException;
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

    private final UserFeignClient userFeignClient;
    private final Keycloak keycloak;
    private final RestTemplate restTemplate;
    private final JwtDecoder jwtDecoder;

    @Value("${kc.server-url}")
    private String serverUrl;
    @Value("${kc.realm}")
    private String realm;
    @Value("${kc.client-id}")
    private String clientId;
    @Value("${kc.client-secret}")
    private String clientSecret;
    @Value("${GOOGLE_REDIRECT_URI}")
    private String googleRedirectUri;


    @Override
    public void save(RegisterRequestDTO registerRequestDTO) {
        UserRepresentation user = createKeycloakUserRepresentation(registerRequestDTO);

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

        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage());
            throw new FailedRegistrationException();
        }
    }

    private UserRepresentation createKeycloakUserRepresentation(RegisterRequestDTO registerRequestDTO) {
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
        return user;
    }

    private LoginResponseDTO fetchToken(MultiValueMap<String, String> params) {
        String url = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.addAll(params);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null) throw new InvalidTokenException();

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setAccessToken((String) responseBody.get("access_token"));
        loginResponseDTO.setRefreshToken((String) responseBody.get("refresh_token"));
        return loginResponseDTO;
    }

    @Override
    public LoginResponseDTO createAuthToken(LoginRequestDTO loginRequestDTO) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", loginRequestDTO.getLogin());
        params.add("password", loginRequestDTO.getPassword());

        return fetchToken(params);
    }

    @Override
    public LoginResponseDTO refreshAuthToken(String refreshToken) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "refresh_token");
        params.add("refresh_token", refreshToken);

        return fetchToken(params);
    }

    @Override
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

    @Override
    public LoginResponseDTO exchangeCodeForToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", googleRedirectUri);

        return fetchToken(params);
    }
}
