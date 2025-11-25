package dev.shrkptv.paymentservice.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCreatedEvent {
    private Long orderId;
    private String status;
}
