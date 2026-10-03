package io.github.pomian99.medical_clinic.dto;

import java.time.LocalDate;

public record PatientCreateCommand (
        String email,
        String password,
        String idCardNo,
        String firstName,
        String lastName,
        String phoneNumber,
        LocalDate birthday
) {
}
