package dev.shrkptv.paymentservice.service.impl;

import dev.shrkptv.paymentservice.client.ExternalAPIClient;
import dev.shrkptv.paymentservice.database.entity.Payment;
import dev.shrkptv.paymentservice.database.enums.PaymentStatus;
import dev.shrkptv.paymentservice.database.repository.PaymentRepository;
import dev.shrkptv.paymentservice.dto.PaymentCreateDTO;
import dev.shrkptv.paymentservice.dto.PaymentResponseDTO;
import dev.shrkptv.paymentservice.mapper.PaymentMapper;
import dev.shrkptv.paymentservice.service.PaymentService;
import dev.shrkptv.paymentservice.service.kafka.PaymentProducer;
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
    private final PaymentProducer paymentProducer;

    @Override
    @Transactional
    public PaymentResponseDTO createPayment(PaymentCreateDTO paymentCreateDTO) {
        try {
            Payment payment = paymentMapper.toEntity(paymentCreateDTO);

            int statusNumber = Integer.parseInt(externalAPIClient.generateRandomNumber().trim());
            PaymentStatus paymentStatus = determinePaymentStatus(statusNumber);
            payment.setStatus(paymentStatus);

            payment.setTimestamp(LocalDateTime.now());

            Payment createdPayment = paymentRepository.save(payment);
            paymentProducer.sendPaymentCreatedEvent(createdPayment);
            return paymentMapper.toDTO(createdPayment);
        }
        catch (NullPointerException e) {
            throw new NullPointerException("External API didn't return anything: " + e.getMessage());
        }
        catch (Exception e) {
            throw new RuntimeException("Exception while creating payment: " + e.getMessage());
        }
    }

    private PaymentStatus determinePaymentStatus(int statusNumber) {
        if (statusNumber % 2 == 0) {
            return PaymentStatus.SUCCESS;
        } else {
            return PaymentStatus.FAILED;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findAllByOrderId(orderId)
                .stream()
                .map(paymentMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByUserId(Long userId) {
        return paymentRepository.findAllByUserId(userId)
                .stream()
                .map(paymentMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getPaymentsByStatuses(List<PaymentStatus> statuses) {
        return paymentRepository.findAllByStatusIn(statuses)
                .stream()
                .map(paymentMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> successfulPayments = paymentRepository.getSuccessfulPayments(startDate, endDate);

        return successfulPayments.stream()
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
