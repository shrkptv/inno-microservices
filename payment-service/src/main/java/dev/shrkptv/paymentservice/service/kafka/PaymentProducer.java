package dev.shrkptv.paymentservice.service.kafka;

import dev.shrkptv.paymentservice.database.entity.Payment;
import dev.shrkptv.paymentservice.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentCreatedEvent(Payment payment) {
        PaymentCreatedEvent event = new PaymentCreatedEvent();
        event.setOrderId(payment.getOrderId());
        event.setStatus(payment.getStatus().name());

        kafkaTemplate.send("payment-events", event);
    }
}
