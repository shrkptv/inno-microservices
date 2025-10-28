package dev.shrkptv.orderservice.dto;

import dev.shrkptv.orderservice.database.enums.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderUpdateDTO {
    @NotBlank(message = "Order status can't be null")
    private OrderStatus orderStatus;
}
