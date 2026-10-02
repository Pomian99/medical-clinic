package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientCreateCommand(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Size(min = 8, message = "Password must contain at least 8 characters")
        String password,

        @NotBlank(message = "ID card number cannot be blank")
        @Pattern(
                regexp = "[A-Z]{3}[0-9]{6}",
                message = "ID card number must contain 3 uppercase letters followed by 6 digits"
        )
        String idCardNo,

        @NotBlank(message = "First name cannot be blank")
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
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
