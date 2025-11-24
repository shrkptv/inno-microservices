package dev.shrkptv.paymentservice.service.kafka;

import dev.shrkptv.paymentservice.database.entity.Payment;
import dev.shrkptv.paymentservice.event.PaymentCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public PaymentProducer(
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${spring.kafka.topics.payments.name}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendPaymentCreatedEvent(Payment payment) {
        PaymentCreatedEvent event = new PaymentCreatedEvent();
        event.setOrderId(payment.getOrderId());
        event.setStatus(payment.getStatus().name());

        kafkaTemplate.send(topic, event);
    }
}
