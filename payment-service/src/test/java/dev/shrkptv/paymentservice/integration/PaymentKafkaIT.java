package dev.shrkptv.paymentservice.integration;

import dev.shrkptv.paymentservice.database.enums.PaymentStatus;
import dev.shrkptv.paymentservice.database.repository.PaymentRepository;
import dev.shrkptv.paymentservice.dto.PaymentCreateDTO;
import dev.shrkptv.paymentservice.dto.PaymentResponseDTO;
import dev.shrkptv.paymentservice.event.PaymentCreatedEvent;
import dev.shrkptv.paymentservice.service.PaymentService;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class PaymentKafkaIT extends AbstractIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ConsumerFactory<String, Object> consumerFactory;

    @Autowired
    private NewTopic paymentEventsTopic;

    void setupMocksExternalAPI() {
        configureFor("localhost", wireMockServer.port());

        stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("24")));
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @AfterEach
    void cleanup() {
        wireMockServer.resetAll();
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("Process order event and create success payment")
    void processSuccessfulPaymentEvent() {
        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setOrderId(1L);
        paymentCreateDTO.setUserId(1L);
        paymentCreateDTO.setPaymentAmount(BigDecimal.valueOf(100.00));

        setupMocksExternalAPI();

        PaymentResponseDTO result = paymentService.createPayment(paymentCreateDTO);

        assertNotNull(result);
        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        assertEquals(paymentCreateDTO.getOrderId(), result.getOrderId());
        assertEquals(paymentCreateDTO.getUserId(), result.getUserId());
        assertEquals(paymentCreateDTO.getPaymentAmount(), result.getPaymentAmount());

        wireMockServer.verify(getRequestedFor(urlPathTemplate("**")));

        var consumer = createTestConsumer();
        consumer.subscribe(Collections.singleton(paymentEventsTopic.name()));

        try {
            await()
                    .atMost(10, TimeUnit.SECONDS)
                    .pollInterval(1, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                var records = consumer.poll(Duration.ofSeconds(1));
                assertFalse(records.isEmpty(), "Kafka event should be sent");

                var record = records.iterator().next();
                assertNotNull(record.value(), "Event value should not be null");

                PaymentCreatedEvent event = (PaymentCreatedEvent) record.value();
                assertEquals(result.getOrderId(), event.getOrderId());
                assertEquals("SUCCESS", event.getStatus());
            });
        } finally {
            consumer.close();
        }
    }

    private Consumer<String, Object> createTestConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return consumerFactory.createConsumer(
                "test-group",
                "test-client",
                null,
                props
        );
    }
}
