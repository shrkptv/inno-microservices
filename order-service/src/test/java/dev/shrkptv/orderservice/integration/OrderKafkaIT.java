package dev.shrkptv.orderservice.integration;

import dev.shrkptv.orderservice.database.entity.Order;
import dev.shrkptv.orderservice.database.enums.OrderStatus;
import dev.shrkptv.orderservice.database.repository.OrderRepository;
import dev.shrkptv.orderservice.event.PaymentCreatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class OrderKafkaIT extends AbstractIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    private static final String TOPIC = "payment-events";

    @AfterEach
    void cleanup() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Process successful payment event and update order status")
    void processSuccessfulPaymentEvent() {
        Order order = new Order();
        order.setUserId(1L);
        order.setOrderStatus(OrderStatus.NEW);
        order.setCreationDate(LocalDate.now());
        Order savedOrder = orderRepository.save(order);

        PaymentCreatedEvent event = new PaymentCreatedEvent();
        event.setOrderId(savedOrder.getId());
        event.setStatus("SUCCESS");

        kafkaTemplate.send(TOPIC, event);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<Order> updatedOrder = orderRepository.findById(savedOrder.getId());
            assertTrue(updatedOrder.isPresent());
            assertEquals(OrderStatus.PAID, updatedOrder.get().getOrderStatus());
        });
    }

    @Test
    @DisplayName("Process failed payment event and update order status")
    void processFailedPaymentEvent() {
        Order order = new Order();
        order.setUserId(1L);
        order.setOrderStatus(OrderStatus.NEW);
        order.setCreationDate(LocalDate.now());
        Order savedOrder = orderRepository.save(order);

        PaymentCreatedEvent event = new PaymentCreatedEvent();
        event.setOrderId(savedOrder.getId());
        event.setStatus("FAILED");

        kafkaTemplate.send(TOPIC, event);
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Optional<Order> updatedOrder = orderRepository.findById(savedOrder.getId());
            assertTrue(updatedOrder.isPresent());
            assertEquals(OrderStatus.PAYMENT_FAILED, updatedOrder.get().getOrderStatus());
        });
    }
}
