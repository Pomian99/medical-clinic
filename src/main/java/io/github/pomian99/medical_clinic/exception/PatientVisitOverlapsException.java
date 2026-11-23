package io.github.pomian99.medical_clinic.exception;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class PatientVisitOverlapsException extends MedicalClinicException {
    public PatientVisitOverlapsException(long patientId, LocalDateTime startsAt, LocalDateTime endsAt) {
        super(
            String.format("Patient with id %d already has a visit overlapping %s - %s", patientId, startsAt, endsAt),
            HttpStatus.CONFLICT
        );
    }
}
