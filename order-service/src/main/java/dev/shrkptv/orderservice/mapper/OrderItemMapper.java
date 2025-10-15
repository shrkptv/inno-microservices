package dev.shrkptv.orderservice.mapper;

import dev.shrkptv.orderservice.database.entity.OrderItem;
import dev.shrkptv.orderservice.dto.OrderItemCreateDTO;
import dev.shrkptv.orderservice.dto.OrderItemResponseDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemResponseDTO toDto(OrderItem orderItem);

    List<OrderItem> toEntityList(List<OrderItemCreateDTO> orderItems);
}
