package dev.shrkptv.paymentservice.service.kafka;

import dev.shrkptv.paymentservice.dto.PaymentCreateDTO;
import dev.shrkptv.paymentservice.event.OrderCreatedEvent;
import dev.shrkptv.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${spring.kafka.topics.orders.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenCreateOrderEvent(OrderCreatedEvent event) {
        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setOrderId(event.getOrderId());
        paymentCreateDTO.setUserId(event.getUserId());
        paymentCreateDTO.setPaymentAmount(event.getTotalAmount());

        paymentService.createPayment(paymentCreateDTO);
    }
}
