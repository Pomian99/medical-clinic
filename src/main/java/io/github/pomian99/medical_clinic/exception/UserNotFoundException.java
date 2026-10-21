package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends MedicalClinicException {
    public UserNotFoundException(long id) {
        super(
            String.format("User with id %d not found", id),
            HttpStatus.NOT_FOUND
        );
    }

    public UserNotFoundException(String email) {
        super(
            String.format("User with email %s not found", email),
            HttpStatus.NOT_FOUND
        );
    }
}
