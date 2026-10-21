package io.github.pomian99.medical_clinic.dto;

public record FacilityDto(
        Long id,
        String name,
        String city,
        String zipCode,
        String street,
        String number
) {
}
