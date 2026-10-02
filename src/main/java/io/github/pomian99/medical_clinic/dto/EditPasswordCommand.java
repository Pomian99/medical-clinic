package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EditPasswordCommand(
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, max = 64, message = "Password must contain between 8 and 64 characters")
        @Pattern(
                regexp = "(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s])\\S+",
                message = "Password must contain a lowercase letter, an uppercase letter, "
                        + "a digit and a special character, and no whitespace"
        )
        String password
) {
}
