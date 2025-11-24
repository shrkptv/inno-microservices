package dev.shrkptv.paymentservice.mapper;

import dev.shrkptv.paymentservice.database.entity.Payment;
import dev.shrkptv.paymentservice.dto.PaymentCreateDTO;
import dev.shrkptv.paymentservice.dto.PaymentResponseDTO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PaymentMapper {
    Payment toEntity(PaymentCreateDTO dto);

    PaymentResponseDTO toDTO(Payment entity);
}
