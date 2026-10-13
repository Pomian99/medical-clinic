package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class FacilityAlreadyExistsException extends MedicalClinicException {
    public FacilityAlreadyExistsException(String name) {
        super(
                String.format("Facility with name %s already exists", name),
                HttpStatus.CONFLICT
        );
    }
}
