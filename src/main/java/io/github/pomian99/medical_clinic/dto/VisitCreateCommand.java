package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record VisitCreateCommand(
        @NotNull(message = "Doctor id is required")
        Long doctorId,

        @NotNull(message = "Start time is required")
        LocalDateTime startsAt,

        @NotNull(message = "End time is required")
        LocalDateTime endsAt
) {
}
