package io.github.pomian99.medical_clinic.dto;

import java.time.LocalDate;

public record PatientUpdateCommand(
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate birthday
) {
}
