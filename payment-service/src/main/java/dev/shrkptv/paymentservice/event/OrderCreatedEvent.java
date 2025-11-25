package dev.shrkptv.paymentservice.event;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderCreatedEvent {
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
}
