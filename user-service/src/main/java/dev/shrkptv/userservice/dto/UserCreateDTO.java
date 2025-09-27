package dev.shrkptv.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserCreateDTO {
    @NotBlank(message = "name can't be empty")
    private String name;

    @NotBlank(message = "surname can't be empty")
    private String surname;

    @Past(message = "birth date must be in the past")
    private LocalDate birthDate;

    @Email(message = "invalid email format")
    @NotBlank(message = "email can't be empty")
    private String email;
}
