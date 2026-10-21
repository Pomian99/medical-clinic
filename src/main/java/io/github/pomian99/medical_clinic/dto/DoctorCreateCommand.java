package io.github.pomian99.medical_clinic.dto;

import io.github.pomian99.medical_clinic.model.Specialization;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import static io.github.pomian99.medical_clinic.dto.ValidationRules.FIRST_NAME_PATTERN_MESSAGE;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.LAST_NAME_PATTERN_MESSAGE;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.NAME_MAX_LENGTH;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.NAME_REGEX;

public record DoctorCreateCommand(
        @NotNull(message = "User id is required")
        Long userId,

        @NotBlank(message = "First name cannot be blank")
        @Size(max = NAME_MAX_LENGTH, message = "First name cannot be longer than {max} characters")
        @Pattern(regexp = NAME_REGEX, message = FIRST_NAME_PATTERN_MESSAGE)
        String firstName,

        @NotBlank(message = "Last name cannot be blank")
        @Size(max = NAME_MAX_LENGTH, message = "Last name cannot be longer than {max} characters")
        @Pattern(regexp = NAME_REGEX, message = LAST_NAME_PATTERN_MESSAGE)
        String lastName,

        @NotNull(message = "Specialization is required")
        Specialization specialization
) {
}
