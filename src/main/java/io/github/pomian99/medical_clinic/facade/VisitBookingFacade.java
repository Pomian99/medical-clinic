package io.github.pomian99.medical_clinic.facade;

import io.github.pomian99.medical_clinic.dto.BookingSummaryDto;
import io.github.pomian99.medical_clinic.dto.DoctorDto;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import io.github.pomian99.medical_clinic.dto.VisitDto;
import io.github.pomian99.medical_clinic.service.DoctorService;
import io.github.pomian99.medical_clinic.service.PatientService;
import io.github.pomian99.medical_clinic.service.VisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VisitBookingFacade {
    private final VisitService visitService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    @Transactional
    public BookingSummaryDto book(Long visitId, Long patientId) {
        VisitDto visit = visitService.book(visitId, patientId);
        PatientDto patient = patientService.findById(patientId);
        DoctorDto doctor = doctorService.findById(visit.doctorId());
        long freeSlots = visitService.countFreeSlots(doctor.id(), visit.startsAt().toLocalDate());
        log.debug("Built booking summary for visit {} and doctor {}", visit.id(), doctor.id());
        return new BookingSummaryDto(
            visit,
            patient,
            doctor,
            freeSlots
        );
    }
}
