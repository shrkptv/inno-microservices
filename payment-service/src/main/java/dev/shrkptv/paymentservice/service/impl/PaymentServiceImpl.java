package dev.shrkptv.paymentservice.service.impl;

import dev.shrkptv.paymentservice.client.ExternalAPIClient;
import dev.shrkptv.paymentservice.database.entity.Payment;
import dev.shrkptv.paymentservice.database.enums.PaymentStatus;
import dev.shrkptv.paymentservice.database.repository.PaymentRepository;
import dev.shrkptv.paymentservice.dto.PaymentCreateDTO;
import dev.shrkptv.paymentservice.dto.PaymentResponseDTO;
import dev.shrkptv.paymentservice.mapper.PaymentMapper;
import dev.shrkptv.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final ExternalAPIClient externalAPIClient;

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentCreateDTO paymentCreateDTO) {
        Payment payment = paymentMapper.toEntity(paymentCreateDTO);
        int statusNumber = Integer.parseInt(externalAPIClient.generateRandomNumber());

        if (statusNumber % 2 == 0) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        payment.setTimestamp(LocalDateTime.now());

        Payment createdPayment = paymentRepository.save(payment);
        return paymentMapper.toDTO(createdPayment);
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findAllByOrderId(orderId)
                .stream()
                .map(paymentMapper::toDTO)
                .toList();
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByUserId(Long userId) {
        return paymentRepository.findAllByUserId(userId)
                .stream()
                .map(paymentMapper::toDTO)
                .toList();
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByStatuses(List<PaymentStatus> statuses) {
        return paymentRepository.findAllByStatusIn(statuses)
                .stream()
                .map(paymentMapper::toDTO)
                .toList();
    }

    @Override
    public BigDecimal getTotalAmountForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalAmount = paymentRepository.getTotalAmount(startDate, endDate);
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }
}
