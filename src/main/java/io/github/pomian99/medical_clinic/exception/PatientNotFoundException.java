package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class PatientNotFoundException extends MedicalClinicException {
    public PatientNotFoundException(long id) {
        super(
                String.format("Patient with id %d not found", id),
                HttpStatus.NOT_FOUND
        );
    }

    public PatientNotFoundException(String email) {
        super(
                String.format("Patient with email %s not found", email),
                HttpStatus.NOT_FOUND
        );
    }
}
