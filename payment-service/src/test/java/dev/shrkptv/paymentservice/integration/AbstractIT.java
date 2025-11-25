package dev.shrkptv.paymentservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.lifecycle.Startables;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIT {

    static MongoDBContainer mongo = new MongoDBContainer("mongo:latest");

    static KafkaContainer kafka = new KafkaContainer("apache/kafka:latest");

    protected static WireMockServer wireMockServer =
            new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    static {
        Startables.deepStart(mongo, kafka).join();
        wireMockServer.start();
    }


    @DynamicPropertySource
    static void registerPostgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");

        registry.add("external-api.url", wireMockServer::baseUrl);
    }
}

