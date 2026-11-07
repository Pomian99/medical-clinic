package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends MedicalClinicException {
    public UserAlreadyExistsException(String email) {
        super(
                String.format("User with email %s already exists", email),
                HttpStatus.CONFLICT
        );
    }
}
