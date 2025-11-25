package dev.shrkptv.orderservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIT {

    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:latest");

    static KafkaContainer kafka = new KafkaContainer("apache/kafka:latest");

    protected static WireMockServer wireMockServer =
            new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    static {
        Startables.deepStart(postgres, kafka).join();
        wireMockServer.start();
    }

    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("user-service.url", wireMockServer::baseUrl);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");
    }
}

