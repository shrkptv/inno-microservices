package dev.shrkptv.orderservice.services;

import dev.shrkptv.orderservice.database.enums.OrderStatus;
import dev.shrkptv.orderservice.dto.OrderCreateDTO;
import dev.shrkptv.orderservice.dto.OrderResponseDTO;
import dev.shrkptv.orderservice.dto.OrderUpdateDTO;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderCreateDTO orderCreateDTO);

    OrderResponseDTO getOrderById(Long id);

    List<OrderResponseDTO> getOrderListById(List<Long> idList);

    List<OrderResponseDTO> getOrderListByStatus(OrderStatus status);

    OrderResponseDTO updateOrder(Long id, OrderUpdateDTO userUpdateDTO);

    void deleteOrder(Long id);
}
