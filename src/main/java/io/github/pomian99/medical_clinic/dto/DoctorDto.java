package io.github.pomian99.medical_clinic.dto;

import io.github.pomian99.medical_clinic.model.Specialization;

public record DoctorDto(
        Long id,
        Long userId,
        String email,
        String firstName,
        String lastName,
        Specialization specialization
) {
}
