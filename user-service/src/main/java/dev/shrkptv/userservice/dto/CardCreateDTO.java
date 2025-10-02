package dev.shrkptv.userservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CardCreateDTO {
    @NotBlank(message = "card number can't be empty")
    private String number;

    @NotBlank
    private String holder;

    @Future(message = "expiration date must be in the future")
    private LocalDate expirationDate;
}
