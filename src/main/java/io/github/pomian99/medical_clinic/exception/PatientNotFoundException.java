package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class PatientNotFoundException extends MedicalClinicException {
    public PatientNotFoundException(long id) {
        super(
                String.format("Patient with id %d not found", id),
                HttpStatus.NOT_FOUND
        );
    }

    public PatientNotFoundException() {
        super(
                "Patient with this email not found",
                HttpStatus.NOT_FOUND
        );
    }
}
