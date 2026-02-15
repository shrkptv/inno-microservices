package dev.shrkptv.orderservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderCreateDTO {
    @Email(message = "Invalid email format")
    private String userEmail;

    @NotEmpty(message = "Order must contain at least one item")
    @Size(min = 1)
    private List<OrderItemCreateDTO> items;
}
