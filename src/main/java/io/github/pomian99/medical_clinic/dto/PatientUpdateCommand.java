package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import static io.github.pomian99.medical_clinic.dto.ValidationRules.EMAIL_MAX_LENGTH;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.FIRST_NAME_PATTERN_MESSAGE;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.LAST_NAME_PATTERN_MESSAGE;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.NAME_MAX_LENGTH;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.NAME_REGEX;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PHONE_NUMBER_PATTERN_MESSAGE;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PHONE_NUMBER_REGEX;

public record PatientUpdateCommand(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid")
        @Size(max = EMAIL_MAX_LENGTH, message = "Email cannot be longer than {max} characters")
        String email,

        @NotBlank(message = "First name cannot be blank")
        @Size(max = NAME_MAX_LENGTH, message = "First name cannot be longer than {max} characters")
        @Pattern(regexp = NAME_REGEX, message = FIRST_NAME_PATTERN_MESSAGE)
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(max = NAME_MAX_LENGTH, message = "Last name cannot be longer than {max} characters")
        @Pattern(regexp = NAME_REGEX, message = LAST_NAME_PATTERN_MESSAGE)
        String lastName,

        @NotBlank(message = "Phone number cannot be blank")
        @Pattern(regexp = PHONE_NUMBER_REGEX, message = PHONE_NUMBER_PATTERN_MESSAGE)
        String phoneNumber,

        @Past(message = "Birthday must be a date in the past")
        LocalDate birthday
) {
}
