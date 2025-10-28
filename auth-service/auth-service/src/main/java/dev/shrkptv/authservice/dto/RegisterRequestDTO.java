package dev.shrkptv.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterRequestDTO {

    @NotBlank(message = "Login can't be blank")
    @Email(message = "Invalid login format")
    private String login;

    @NotBlank(message = "Password can't be blank")
    private String password;

    @NotBlank(message = "Name can't be empty")
    private String name;

    @NotBlank(message = "Surname can't be empty")
    private String surname;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
}
