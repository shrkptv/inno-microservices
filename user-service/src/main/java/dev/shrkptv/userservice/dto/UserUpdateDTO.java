package dev.shrkptv.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserUpdateDTO {
    private String name;
    private String surname;

    @Past(message = "birth date must be in the past")
    private LocalDate birthDate;

    @Email(message = "invalid email format")
    private String email;
}
