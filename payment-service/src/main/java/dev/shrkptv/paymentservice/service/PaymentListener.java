package dev.shrkptv.paymentservice.service;

import dev.shrkptv.paymentservice.dto.PaymentCreateDTO;
import dev.shrkptv.paymentservice.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentListener {

    private final PaymentService paymentService;

    @KafkaListener(topics = "order-events", groupId = "payment-service-group")
    public void listenCreateOrderEvent(OrderCreatedEvent event) {
        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setOrderId(event.getOrderId());
        paymentCreateDTO.setUserId(event.getUserId());
        paymentCreateDTO.setAmount(event.getTotalAmount());

        paymentService.createPayment(paymentCreateDTO);
    }
}
