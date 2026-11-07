package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class DoctorAlreadyExistsException extends MedicalClinicException {
    public DoctorAlreadyExistsException(long userId) {
        super(
                String.format("User with id %d already has a doctor", userId),
                HttpStatus.CONFLICT
        );
    }
}
