package io.github.pomian99.medical_clinic.facade;

import io.github.pomian99.medical_clinic.dto.BookingSummaryDto;
import io.github.pomian99.medical_clinic.mapper.DoctorMapper;
import io.github.pomian99.medical_clinic.mapper.PatientMapper;
import io.github.pomian99.medical_clinic.mapper.VisitMapper;
import io.github.pomian99.medical_clinic.model.Doctor;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.model.Visit;
import io.github.pomian99.medical_clinic.service.DoctorService;
import io.github.pomian99.medical_clinic.service.PatientService;
import io.github.pomian99.medical_clinic.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VisitBookingFacade {
    private final VisitService visitService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final VisitMapper visitMapper;
    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;

    @Transactional
    public BookingSummaryDto book(Long visitId, Long patientId) {
        Visit visit = visitService.book(visitId, patientId);
        Patient patient = patientService.findById(patientId);
        Doctor doctor = doctorService.findById(visit.getDoctor().getId());
        long freeSlots = visitService.countFreeSlots(doctor.getId(), visit.getStartsAt().toLocalDate());
        return new BookingSummaryDto(
            visitMapper.toDto(visit),
            patientMapper.toDto(patient),
            doctorMapper.toDto(doctor),
            freeSlots
        );
    }
}
