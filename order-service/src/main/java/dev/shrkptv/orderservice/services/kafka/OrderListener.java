package dev.shrkptv.orderservice.services.kafka;

import dev.shrkptv.orderservice.database.enums.OrderStatus;
import dev.shrkptv.orderservice.database.repository.OrderRepository;
import dev.shrkptv.orderservice.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderListener {
    private final OrderRepository orderRepository;

    @KafkaListener(topics = "${spring.kafka.topics.payments.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenCreatePaymentEvent(PaymentCreatedEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setOrderStatus(event.getStatus().equals("SUCCESS") ? OrderStatus.PAID : OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
        });
    }
}
