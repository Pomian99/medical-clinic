package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class VisitNotFoundException extends MedicalClinicException {
    public VisitNotFoundException(long id) {
        super(
            String.format("Visit with id %d not found", id),
            HttpStatus.NOT_FOUND
        );
    }
}
