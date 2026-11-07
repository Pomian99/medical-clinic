package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class UserInUseException extends MedicalClinicException{
    public UserInUseException(long id) {
        super(
            String.format("User with id %d is in use", id),
            HttpStatus.CONFLICT
        );
    }
}
