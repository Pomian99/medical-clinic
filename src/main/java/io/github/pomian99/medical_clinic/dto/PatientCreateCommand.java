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
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PASSWORD_MAX_LENGTH;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PASSWORD_MIN_LENGTH;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PASSWORD_PATTERN_MESSAGE;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PASSWORD_REGEX;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PHONE_NUMBER_PATTERN_MESSAGE;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PHONE_NUMBER_REGEX;

public record PatientCreateCommand(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email must be valid")
        @Size(max = EMAIL_MAX_LENGTH, message = "Email cannot be longer than {max} characters")
        String email,

        @NotBlank(message = "Password cannot be blank")
        @Size(
                min = PASSWORD_MIN_LENGTH,
                max = PASSWORD_MAX_LENGTH,
                message = "Password must contain between {min} and {max} characters"
        )
        @Pattern(regexp = PASSWORD_REGEX, message = PASSWORD_PATTERN_MESSAGE)
        String password,

        @NotBlank(message = "ID card number cannot be blank")
        @Pattern(
                regexp = "[A-Z]{3}[0-9]{6}",
                message = "ID card number must contain 3 uppercase letters followed by 6 digits"
        )
        String idCardNo,

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
