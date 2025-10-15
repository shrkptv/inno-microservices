package dev.shrkptv.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemUpdateDTO {
    @NotBlank(message = "Quantity can't be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
