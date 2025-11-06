package dev.shrkptv.authservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserCreateRequestDTO {
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
}
