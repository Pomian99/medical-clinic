package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

public class VisitAlreadyBookedException extends MedicalClinicException {
    public VisitAlreadyBookedException(long id) {
        super(
            String.format("Visit with id %d is already booked", id),
            HttpStatus.CONFLICT
        );
    }
}
