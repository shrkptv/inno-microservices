package dev.shrkptv.paymentservice.service;

import dev.shrkptv.paymentservice.database.enums.PaymentStatus;
import dev.shrkptv.paymentservice.dto.PaymentCreateDTO;
import dev.shrkptv.paymentservice.dto.PaymentResponseDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {
    PaymentResponseDTO createPayment(PaymentCreateDTO paymentCreateDTO);

    List<PaymentResponseDTO> getPaymentsByOrderId(Long orderId);

    List<PaymentResponseDTO> getPaymentsByUserId(Long userId);

    List<PaymentResponseDTO> getPaymentsByStatuses(List<PaymentStatus> statuses);

    BigDecimal getTotalAmountForPeriod(LocalDateTime startDate, LocalDateTime endDate);
}
