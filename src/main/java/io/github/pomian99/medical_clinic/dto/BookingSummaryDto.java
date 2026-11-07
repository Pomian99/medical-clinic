package io.github.pomian99.medical_clinic.dto;

public record BookingSummaryDto(
    VisitDto visit,
    PatientDto patient,
    DoctorDto doctor,
    long freeSlotsThatDay
) {
}
