package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class InvalidSubstitutionException extends MedicalClinicException {
    public InvalidSubstitutionException(long doctorId) {
        super(
            String.format("Doctor with id %d cannot substitute for themselves", doctorId),
            HttpStatus.BAD_REQUEST
        );
    }
}
