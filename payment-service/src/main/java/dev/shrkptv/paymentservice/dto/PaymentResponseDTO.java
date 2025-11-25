package dev.shrkptv.paymentservice.dto;

import dev.shrkptv.paymentservice.database.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaymentResponseDTO {
    private String id;
    private Long orderId;
    private Long userId;
    private PaymentStatus status;
    private LocalDateTime timestamp;
    private BigDecimal paymentAmount;
}
