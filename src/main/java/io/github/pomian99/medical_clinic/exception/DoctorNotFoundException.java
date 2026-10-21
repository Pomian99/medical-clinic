package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class DoctorNotFoundException extends MedicalClinicException {
    public DoctorNotFoundException(long id) {
        super(
                String.format("Doctor with id %d not found", id),
                HttpStatus.NOT_FOUND
        );
    }
}
