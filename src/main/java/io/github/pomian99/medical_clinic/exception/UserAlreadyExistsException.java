package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends MedicalClinicException {
    public UserAlreadyExistsException() {
        super(
                "User with this email already exists",
                HttpStatus.CONFLICT
        );
    }
}
