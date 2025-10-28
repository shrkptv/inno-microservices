package dev.shrkptv.orderservice.mapper;

import dev.shrkptv.orderservice.database.entity.Order;
import dev.shrkptv.orderservice.dto.OrderCreateDTO;
import dev.shrkptv.orderservice.dto.OrderResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(source = "orderStatus", target = "status")
    OrderResponseDTO toDto(Order order);

    Order toEntity(OrderCreateDTO orderCreateDTO);

    List<OrderResponseDTO> toDtoList(List<Order> orders);
}
