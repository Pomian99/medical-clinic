package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class PatientAlreadyExistsException extends MedicalClinicException {
    public PatientAlreadyExistsException() {
        super(
            "Patient with this ID card number already exists",
            HttpStatus.CONFLICT
        );
    }
}
