package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditPasswordCommand(
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "Password must contain at least 8 characters")
        String password
) {
}
