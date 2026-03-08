package dev.shrkptv.userservice.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Value("${kc.server-url}")
    private String serverUrl;

    @Value("${kc.realm}")
    private String realm;

    @Value("${kc.client-id}")
    private String clientId;

    @Value("${kc.client-secret:}")
    private String clientSecret;

    @Value("${kc.username:}")
    private String username;

    @Value("${kc.password:}")
    private String password;

    @Bean
    public Keycloak keycloak() {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId);

        if (username != null && !username.isBlank()) {
            builder.grantType("password")
                    .username(username)
                    .password(password);
        } else {
            builder.grantType("client_credentials")
                    .clientSecret(clientSecret);
        }

        return builder.build();
    }
}