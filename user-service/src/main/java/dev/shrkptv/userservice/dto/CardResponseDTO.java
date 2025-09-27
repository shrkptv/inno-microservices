package dev.shrkptv.userservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CardResponseDTO {
    private Long id;
    private String number;
    private String holder;
    private LocalDate expirationDate;
}
