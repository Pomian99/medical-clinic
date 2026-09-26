package io.github.pomian99.medical_clinic.dto;

import io.github.pomian99.medical_clinic.model.Patient;

import java.time.LocalDate;

public record PatientDto(
    Long id,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    LocalDate birthday
) {
    public static PatientDto from(Patient patient) {
        return new PatientDto(
                patient.getId(),
                patient.getEmail(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getPhoneNumber(),
                patient.getBirthday()
        );
    }
}
