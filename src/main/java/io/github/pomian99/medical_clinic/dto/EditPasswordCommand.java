package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static io.github.pomian99.medical_clinic.dto.ValidationRules.PASSWORD_MAX_LENGTH;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PASSWORD_MIN_LENGTH;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PASSWORD_PATTERN_MESSAGE;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PASSWORD_REGEX;

public record EditPasswordCommand(
        @NotBlank(message = "Password cannot be blank")
        @Size(
                min = PASSWORD_MIN_LENGTH,
                max = PASSWORD_MAX_LENGTH,
                message = "Password must contain between {min} and {max} characters"
        )
        @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_PATTERN_MESSAGE)
        String password
) {
}
