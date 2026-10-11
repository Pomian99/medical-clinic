package io.github.pomian99.medical_clinic.dto;

import java.time.LocalDateTime;

public record VisitDto(
        Long id,
        Long doctorId,
        Long patientId,
        LocalDateTime startsAt,
        LocalDateTime endsAt
) {
}
