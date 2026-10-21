package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class DoctorHasVisitsException extends MedicalClinicException {
    public DoctorHasVisitsException(long id) {
        super(
                String.format("Doctor with id %d has visits and cannot be deleted", id),
                HttpStatus.CONFLICT
        );
    }
}
