package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class InvalidVisitException extends MedicalClinicException {
    public InvalidVisitException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
