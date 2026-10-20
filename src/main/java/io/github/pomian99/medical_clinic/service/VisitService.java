package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.VisitCreateCommand;
import io.github.pomian99.medical_clinic.exception.DoctorNotFoundException;
import io.github.pomian99.medical_clinic.mapper.VisitMapper;
import io.github.pomian99.medical_clinic.model.Doctor;
import io.github.pomian99.medical_clinic.model.Visit;
import io.github.pomian99.medical_clinic.repository.DoctorRepository;
import io.github.pomian99.medical_clinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final VisitMapper mapper;

    public Visit create(VisitCreateCommand command) {
        Doctor doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(command.doctorId()));

        // A new visit is a free slot: it has a doctor, but no patient yet.
        Visit visit = mapper.toEntity(command);
        visit.setDoctor(doctor);

        return visitRepository.save(visit);
    }

    public List<Visit> findAllByDoctorId(long id) {
        return visitRepository.findAllByDoctorId(id);
    }
}
