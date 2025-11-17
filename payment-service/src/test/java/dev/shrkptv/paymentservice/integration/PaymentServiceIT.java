package dev.shrkptv.paymentservice.integration;

import dev.shrkptv.paymentservice.database.entity.Payment;
import dev.shrkptv.paymentservice.database.enums.PaymentStatus;
import dev.shrkptv.paymentservice.database.repository.PaymentRepository;
import dev.shrkptv.paymentservice.dto.PaymentCreateDTO;
import dev.shrkptv.paymentservice.dto.PaymentResponseDTO;
import dev.shrkptv.paymentservice.service.PaymentService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentServiceIT extends AbstractIT {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @AfterEach
    void cleanup() {
        wireMockServer.resetAll();
        paymentRepository.deleteAll();
    }

    void setupMocksExternalAPI(String responseNumber) {
        configureFor("localhost", wireMockServer.port());

        stubFor(get(anyUrl())
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody(responseNumber)));
    }

    @Test
    @DisplayName("Create new success payment")
    void createSuccessPayment() {
        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setOrderId(1L);
        paymentCreateDTO.setUserId(1L);
        paymentCreateDTO.setPaymentAmount(BigDecimal.valueOf(100.00));

        setupMocksExternalAPI("42");

        PaymentResponseDTO result = paymentService.createPayment(paymentCreateDTO);

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        assertEquals(paymentCreateDTO.getOrderId(), result.getOrderId());
        assertEquals(paymentCreateDTO.getUserId(), result.getUserId());
        assertEquals(paymentCreateDTO.getPaymentAmount(), result.getPaymentAmount());

        wireMockServer.verify(getRequestedFor(urlPathTemplate("**")));
    }

    @Test
    @DisplayName("Create new failed payments")
    void createFailedPayment() {
        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setOrderId(1L);
        paymentCreateDTO.setUserId(1L);
        paymentCreateDTO.setPaymentAmount(BigDecimal.valueOf(100.00));

        setupMocksExternalAPI("41");

        PaymentResponseDTO result = paymentService.createPayment(paymentCreateDTO);

        assertEquals(PaymentStatus.FAILED, result.getStatus());
        assertEquals(paymentCreateDTO.getOrderId(), result.getOrderId());
        assertEquals(paymentCreateDTO.getUserId(), result.getUserId());
        assertEquals(paymentCreateDTO.getPaymentAmount(), result.getPaymentAmount());

        wireMockServer.verify(getRequestedFor(urlPathTemplate("**")));
    }

    @Test
    @DisplayName("Get list of payments by order id")
    void getPaymentsByOrderId() {
        Payment firstPayment = new Payment();
        firstPayment.setOrderId(1L);

        Payment secondPayment = new Payment();
        secondPayment.setOrderId(1L);

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        List<PaymentResponseDTO> result = paymentService.getPaymentsByOrderId(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getOrderId().equals(1L)));
    }

    @Test
    @DisplayName("Get list of payments by user id")
    void getPaymentsByUserId() {
        Payment firstPayment = new Payment();
        firstPayment.setUserId(1L);

        Payment secondPayment = new Payment();
        secondPayment.setUserId(1L);

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        List<PaymentResponseDTO> result = paymentService.getPaymentsByUserId(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getUserId().equals(1L)));
    }

    @Test
    @DisplayName("Get list of payments by statuses")
    void getPaymentsByStatuses() {
        Payment firstPayment = new Payment();
        firstPayment.setStatus(PaymentStatus.SUCCESS);

        Payment secondPayment = new Payment();
        secondPayment.setStatus(PaymentStatus.FAILED);

        Payment thirdPayment = new Payment();
        thirdPayment.setStatus(PaymentStatus.SUCCESS);

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        List<PaymentResponseDTO> result = paymentService.getPaymentsByStatuses(
                List.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED));

        assertEquals(2, result.size());
        assertTrue(result.stream()
                .anyMatch(p -> p.getStatus().equals(PaymentStatus.SUCCESS)));
        assertTrue(result.stream()
                .anyMatch(p -> p.getStatus().equals(PaymentStatus.FAILED)));
    }

    @Test
    @DisplayName("Calculate total amount for period")
    void getTotalAmountForPeriod() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        Payment firstPayment = new Payment();
        firstPayment.setPaymentAmount(BigDecimal.valueOf(100));
        firstPayment.setTimestamp(LocalDateTime.now());
        firstPayment.setStatus(PaymentStatus.SUCCESS);

        Payment secondPayment = new Payment();
        secondPayment.setPaymentAmount(BigDecimal.valueOf(50));
        secondPayment.setTimestamp(LocalDateTime.now());
        secondPayment.setStatus(PaymentStatus.FAILED);

        Payment thirdPayment = new Payment();
        thirdPayment.setPaymentAmount(BigDecimal.valueOf(25));
        thirdPayment.setTimestamp(LocalDateTime.now().minusDays(1));
        thirdPayment.setStatus(PaymentStatus.SUCCESS);

        Payment fourthPayment = new Payment();
        fourthPayment.setPaymentAmount(BigDecimal.valueOf(100));
        fourthPayment.setTimestamp(LocalDateTime.now().minusDays(4));
        fourthPayment.setStatus(PaymentStatus.SUCCESS);

        paymentRepository.saveAll(List.of(firstPayment, secondPayment, thirdPayment, fourthPayment));

        BigDecimal result = paymentService.getTotalAmountForPeriod(startDate, endDate);

        assertEquals(BigDecimal.valueOf(125), result);
    }
}
