package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class FacilityNotFoundException extends MedicalClinicException {
    public FacilityNotFoundException(long id) {
        super(
            String.format("Facility with id %d not found", id),
            HttpStatus.NOT_FOUND
        );
    }
}
