package dev.shrkptv.paymentservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TotalAmountDTO {
    BigDecimal totalAmount;
}
