package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.constraints.NotNull;

public record SubstitutionCommand(
    @NotNull(message = "Substitute id is required")
    Long substituteId
) {
}
