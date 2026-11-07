package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginCommand(
    @NotBlank(message = "Email cannot be blank")
    String email,

    @NotBlank(message = "Password cannot be blank")
    String password
) {
}
