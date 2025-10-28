package dev.shrkptv.orderservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponseDTO {
    private Long id;
    private Long itemId;
    private Integer quantity;
    private ItemResponseDTO item;
}
