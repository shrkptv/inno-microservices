package dev.shrkptv.orderservice.dto;

import dev.shrkptv.orderservice.database.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponseDTO {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private List<OrderItemResponseDTO> items;
    private UserResponseDTO user;
}
