package ru.nextupvamp.model.data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserDto(
        @NotBlank(message = "Login is mandatory")
        @Size(min = 6, message = "Login is too short")
        @Size(max = 20, message = "Login is too long")
        String login,

        @NotBlank(message = "Password is mandatory")
        @Size(min = 8, message = "Login is too short")
        @Size(max = 20, message = "Login is too long")
        String password) {
}
