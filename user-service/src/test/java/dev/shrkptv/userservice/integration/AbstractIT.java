package dev.shrkptv.userservice.integration;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIT {

    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:latest");

    static RedisContainer redis =
            new RedisContainer("redis:latest");

    static GenericContainer<?> keycloak = new GenericContainer<>("quay.io/keycloak/keycloak:latest")
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withCommand("start-dev");

    static {
        Startables.deepStart(postgres, redis, keycloak).join();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getRedisPort);

        String keycloakUrl = "http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(8080);
        registry.add("kc.server-url", () -> keycloakUrl);
        registry.add("kc.realm", () -> "master");
        registry.add("kc.client-id", () -> "admin-cli");
        registry.add("kc.username", () -> "admin");
        registry.add("kc.password", () -> "admin");
        registry.add("KC_CLIENT_SECRET", () -> "");
    }

}
