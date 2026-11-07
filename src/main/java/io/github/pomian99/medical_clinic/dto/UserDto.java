package io.github.pomian99.medical_clinic.dto;

import io.github.pomian99.medical_clinic.model.Role;

public record UserDto(
    Long id,
    String email,
    Role role
) {
}
