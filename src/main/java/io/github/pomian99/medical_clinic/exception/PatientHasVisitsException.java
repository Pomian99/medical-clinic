package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class PatientHasVisitsException extends MedicalClinicException {
    public PatientHasVisitsException(long id) {
        super(
            String.format("Patient with id %d has visits and cannot be deleted", id),
            HttpStatus.CONFLICT
        );
    }
}
