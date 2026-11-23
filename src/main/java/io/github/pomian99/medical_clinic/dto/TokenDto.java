package io.github.pomian99.medical_clinic.dto;

public record TokenDto(
    String token,
    String tokenType,
    long expiresIn
) {
}
