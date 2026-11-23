package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static io.github.pomian99.medical_clinic.dto.ValidationRules.EMAIL_MAX_LENGTH;

public record UserUpdateCommand(
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    @Size(max = EMAIL_MAX_LENGTH, message = "Email cannot be longer than {max} characters")
    String email
) {
}
