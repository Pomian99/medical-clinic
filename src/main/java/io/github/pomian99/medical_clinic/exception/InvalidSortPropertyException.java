package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

import java.util.List;

public class InvalidSortPropertyException extends MedicalClinicException {
    public InvalidSortPropertyException(String property, List<String> allowed) {
        super(
            String.format("Cannot sort by '%s', allowed: %s", property, String.join(", ", allowed)),
            HttpStatus.BAD_REQUEST
        );
    }
}
