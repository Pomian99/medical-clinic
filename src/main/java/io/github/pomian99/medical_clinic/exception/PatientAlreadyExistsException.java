package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class PatientAlreadyExistsException extends MedicalClinicException {
    public PatientAlreadyExistsException(String email) {
        super(
                String.format("Patient with email %s already exists", email),
                HttpStatus.CONFLICT
        );
    }
}
