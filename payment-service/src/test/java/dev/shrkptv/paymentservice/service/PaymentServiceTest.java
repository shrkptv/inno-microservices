package dev.shrkptv.paymentservice.service;

import dev.shrkptv.paymentservice.client.ExternalAPIClient;
import dev.shrkptv.paymentservice.database.entity.Payment;
import dev.shrkptv.paymentservice.database.enums.PaymentStatus;
import dev.shrkptv.paymentservice.database.repository.PaymentRepository;
import dev.shrkptv.paymentservice.dto.PaymentCreateDTO;
import dev.shrkptv.paymentservice.dto.PaymentResponseDTO;
import dev.shrkptv.paymentservice.mapper.PaymentMapper;
import dev.shrkptv.paymentservice.service.impl.PaymentServiceImpl;
import dev.shrkptv.paymentservice.service.kafka.PaymentProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private ExternalAPIClient externalAPIClient;
    @Mock
    private PaymentProducer paymentProducer;
    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Create new success payment")
    void createPayment_whenEvenNumber_setStatusSuccess() {
        PaymentCreateDTO paymentCreateDTO = createDTO(1L, 2L, BigDecimal.valueOf(222.0));
        Payment payment = createEntity(1L, 2L, BigDecimal.valueOf(222.0));
        PaymentResponseDTO paymentResponseDTO = createResponse(1L, 2L, BigDecimal.valueOf(222.0),
                PaymentStatus.SUCCESS);

        when(paymentMapper.toEntity(paymentCreateDTO)).thenReturn(payment);
        when(externalAPIClient.generateRandomNumber()).thenReturn("50");
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDTO(payment)).thenReturn(paymentResponseDTO);

        PaymentResponseDTO result = paymentService.createPayment(paymentCreateDTO);

        assertNotNull(result);
        assertEquals(paymentResponseDTO.getStatus(), result.getStatus());

        verify(paymentMapper).toEntity(paymentCreateDTO);
        verify(externalAPIClient).generateRandomNumber();
        verify(paymentRepository).save(payment);
        verify(paymentMapper).toDTO(payment);
        verify(paymentProducer).sendPaymentCreatedEvent(payment);
    }

    @Test
    @DisplayName("Create new failed payment")
    void createPayment_whenOddNumber_setStatusFailed() {
        PaymentCreateDTO paymentCreateDTO = createDTO(10L, 20L, BigDecimal.valueOf(99.99));
        Payment payment = createEntity(10L, 20L, BigDecimal.valueOf(99.99));
        PaymentResponseDTO paymentResponseDTO = createResponse(10L, 20L, BigDecimal.valueOf(99.99),
                PaymentStatus.FAILED);

        when(paymentMapper.toEntity(paymentCreateDTO)).thenReturn(payment);
        when(externalAPIClient.generateRandomNumber()).thenReturn("55");
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDTO(payment)).thenReturn(paymentResponseDTO);

        PaymentResponseDTO result = paymentService.createPayment(paymentCreateDTO);

        assertNotNull(result);
        assertEquals(paymentResponseDTO.getStatus(), result.getStatus());

        verify(paymentProducer).sendPaymentCreatedEvent(payment);
    }

    @Test
    @DisplayName("Create payment when external API returns null")
    void createPayment_throwNullPointerException_whenExternalAPIReturnsNull() {
        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setOrderId(1L);
        paymentCreateDTO.setUserId(1L);
        paymentCreateDTO.setPaymentAmount(BigDecimal.valueOf(100.00));

        when(externalAPIClient.generateRandomNumber()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> paymentService.createPayment(paymentCreateDTO));
    }

    private PaymentCreateDTO createDTO(Long orderId, Long userId, BigDecimal amount) {
        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setOrderId(orderId);
        paymentCreateDTO.setUserId(userId);
        paymentCreateDTO.setPaymentAmount(amount);
        return paymentCreateDTO;
    }

    private Payment createEntity(Long orderId, Long userId, BigDecimal amount) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setPaymentAmount(amount);
        return payment;
    }

    private PaymentResponseDTO createResponse(Long orderId, Long userId, BigDecimal amount, PaymentStatus status) {
        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setOrderId(orderId);
        paymentResponseDTO.setUserId(userId);
        paymentResponseDTO.setPaymentAmount(amount);
        paymentResponseDTO.setTimestamp(LocalDateTime.now());
        paymentResponseDTO.setStatus(status);
        return paymentResponseDTO;
    }

    @Test
    @DisplayName("Return list of payments by order id")
    void getPaymentsByOrderId_returnsListForSpecificOrderId() {
        Payment payment = new Payment();
        payment.setOrderId(1L);

        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setOrderId(1L);

        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(paymentResponseDTO);

        List<PaymentResponseDTO> result = paymentService.getPaymentsByOrderId(1L);

        assertEquals(1, result.size());
        verify(paymentRepository).findAllByOrderId(1L);
        verify(paymentMapper).toDTO(payment);
    }

    @Test
    @DisplayName("Return empty list when order id has no payments")
    void getPaymentsByOrderId_returnsEmptyList() {
        when(paymentRepository.findAllByOrderId(1L)).thenReturn(List.of());
        assertTrue(paymentService.getPaymentsByOrderId(1L).isEmpty());
        verify(paymentRepository).findAllByOrderId(1L);
    }

    @Test
    @DisplayName("Return list of payments by user id")
    void getPaymentsByUserId_returnsListForSpecificUserId() {
        Payment payment = new Payment();
        payment.setUserId(1L);

        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setUserId(1L);

        when(paymentRepository.findAllByUserId(1L)).thenReturn(List.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(paymentResponseDTO);

        List<PaymentResponseDTO> result = paymentService.getPaymentsByUserId(1L);

        assertEquals(1, result.size());
        verify(paymentRepository).findAllByUserId(1L);
        verify(paymentMapper).toDTO(payment);
    }

    @Test
    @DisplayName("Return empty list when user id has no payments")
    void getPaymentsByUserId_returnsEmptyList() {
        when(paymentRepository.findAllByUserId(1L)).thenReturn(List.of());
        assertTrue(paymentService.getPaymentsByUserId(1L).isEmpty());
        verify(paymentRepository).findAllByUserId(1L);
    }

    @Test
    @DisplayName("Return list of payments by status list")
    void getPaymentsByStatuses_returnsListForSpecificStatus() {
        List<PaymentStatus> statusList = new ArrayList<>();

        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.SUCCESS);

        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findAllByStatusIn(statusList)).thenReturn(List.of(payment));
        when(paymentMapper.toDTO(payment)).thenReturn(paymentResponseDTO);

        List<PaymentResponseDTO> result = paymentService.getPaymentsByStatuses(statusList);

        assertEquals(1, result.size());
        verify(paymentRepository).findAllByStatusIn(statusList);
        verify(paymentMapper).toDTO(payment);
    }

    @Test
    @DisplayName("Return empty list when status list has no payments")
    void getPaymentsByStatuses_returnsEmptyList() {
        when(paymentRepository.findAllByStatusIn(List.of(PaymentStatus.FAILED))).thenReturn(List.of());
        assertTrue(paymentService.getPaymentsByStatuses(List.of(PaymentStatus.FAILED)).isEmpty());
        verify(paymentRepository).findAllByStatusIn(List.of(PaymentStatus.FAILED));
    }

    @Test
    @DisplayName("Return total amount of payments by date")
    void getTotalAmountForPeriod_returnsTotalAmount() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        Payment firstPayment = createEntity(1L, 1L, BigDecimal.valueOf(100.0));
        firstPayment.setStatus(PaymentStatus.SUCCESS);

        Payment secondPayment = createEntity(2L, 1L, BigDecimal.valueOf(150.0));
        secondPayment.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.getSuccessfulPayments(startDate, endDate)).
                thenReturn(List.of(firstPayment, secondPayment));

        BigDecimal result = paymentService.getTotalAmountForPeriod(startDate, endDate);

        assertEquals(BigDecimal.valueOf(250.0), result);
        verify(paymentRepository).getSuccessfulPayments(startDate, endDate);
    }

    @Test
    @DisplayName("Return zero when there are no payments in the selected period")
    void getTotalAmountForPeriod_whenNull_returnsZero() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        when(paymentRepository.getSuccessfulPayments(startDate, endDate))
                .thenReturn(List.of());

        BigDecimal result = paymentService.getTotalAmountForPeriod(startDate, endDate);

        assertEquals(BigDecimal.ZERO, result);
        verify(paymentRepository).getSuccessfulPayments(startDate, endDate);
    }
}
