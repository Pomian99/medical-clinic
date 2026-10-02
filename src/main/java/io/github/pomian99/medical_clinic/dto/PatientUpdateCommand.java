package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientUpdateCommand(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid")
        @Size(max = 254, message = "Email cannot be longer than 254 characters")
        String email,

        @NotBlank(message = "First name cannot be blank")
        @Size(max = 50, message = "First name cannot be longer than 50 characters")
        @Pattern(
                regexp = "\\p{L}+([ '-]\\p{L}+)*",
                message = "First name may contain only letters, spaces, hyphens and apostrophes"
        )
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(max = 50, message = "Last name cannot be longer than 50 characters")
        @Pattern(
                regexp = "\\p{L}+([ '-]\\p{L}+)*",
                message = "Last name may contain only letters, spaces, hyphens and apostrophes"
        )
        String lastName,

        @NotBlank(message = "Phone number cannot be blank")
        @Pattern(
                regexp = "\\d{9}",
                message = "Phone number must contain exactly 9 digits"
        )
        String phoneNumber,

        @Past(message = "Birthday must be a date in the past")
        LocalDate birthday
) {
}
