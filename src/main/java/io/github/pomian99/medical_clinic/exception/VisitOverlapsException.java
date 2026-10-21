package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class VisitOverlapsException extends MedicalClinicException {
    public VisitOverlapsException(long doctorId, LocalDateTime startsAt, LocalDateTime endsAt) {
        super(
            String.format("Doctor with id %d already has a visit overlapping %s - %s", doctorId, startsAt, endsAt),
            HttpStatus.CONFLICT
        );
    }
}
