package dev.shrkptv.userservice.dto;

import jakarta.validation.constraints.Future;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CardUpdateDTO {
    private String holder;

    @Future
    private LocalDate expirationDate;
}
