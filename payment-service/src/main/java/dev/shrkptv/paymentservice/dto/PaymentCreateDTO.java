package dev.shrkptv.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentCreateDTO {
    @NotBlank(message = "The payment must relate to some kind of order ID")
    private Long orderId;
    @NotBlank(message = "The payment must relate to some kind of user ID")
    private Long userId;

    @Positive(message = "The amount is greater than zero")
    private BigDecimal paymentAmount;
}
