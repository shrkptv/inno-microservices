package dev.shrkptv.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank(message = "Login can't be blank")
    @Email(message = "Invalid login format")
    private String login;

    @NotBlank(message = "Password can't be blank")
    private String password;
}
