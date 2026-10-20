package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.VisitCreateCommand;
import io.github.pomian99.medical_clinic.exception.DoctorNotFoundException;
import io.github.pomian99.medical_clinic.exception.InvalidVisitException;
import io.github.pomian99.medical_clinic.exception.PatientNotFoundException;
import io.github.pomian99.medical_clinic.exception.VisitAlreadyBookedException;
import io.github.pomian99.medical_clinic.exception.VisitNotFoundException;
import io.github.pomian99.medical_clinic.exception.VisitOverlapsException;
import io.github.pomian99.medical_clinic.mapper.VisitMapper;
import io.github.pomian99.medical_clinic.model.Doctor;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.model.Visit;
import io.github.pomian99.medical_clinic.repository.DoctorRepository;
import io.github.pomian99.medical_clinic.repository.PatientRepository;
import io.github.pomian99.medical_clinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitService {
    private static final Duration SLOT = Duration.ofMinutes(15);

    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final VisitMapper mapper;
    private final Clock clock;

    public Visit create(VisitCreateCommand command) {
        validateSlot(command.startsAt(), command.endsAt());

        Doctor doctor = doctorRepository.findById(command.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(command.doctorId()));

        if (visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(command.doctorId(), command.endsAt(), command.startsAt())) {
            throw new VisitOverlapsException(command.doctorId(), command.startsAt(), command.endsAt());
        }

        // A new visit is a free slot: it has a doctor, but no patient yet.
        Visit visit = mapper.toEntity(command);
        visit.setDoctor(doctor);

        return visitRepository.save(visit);
    }

    public Visit book(long visitId, long patientId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException(visitId));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId));

        if (isPast(visit.getStartsAt())) {
            throw new InvalidVisitException(String.format("Visit with id %d is in the past and cannot be booked", visitId));
        }
        if (!visit.isFree()) {
            throw new VisitAlreadyBookedException(visitId);
        }

        visit.setPatient(patient);
        return visitRepository.save(visit);
    }

    public List<Visit> findAllByDoctorId(long id) {
        return visitRepository.findAllByDoctorId(id);
    }

    private boolean isPast(LocalDateTime time) {
        return time.isBefore(LocalDateTime.now(clock));
    }

    private void validateSlot(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (isPast(startsAt)) {
            throw new InvalidVisitException("Visit start must not be in the past");
        }
        if (startsAt.getMinute() % SLOT.toMinutes() != 0 || startsAt.getSecond() != 0 || startsAt.getNano() != 0) {
            throw new InvalidVisitException("Visit must start at :00, :15, :30 or :45 with zero seconds");
        }
        Duration length = Duration.between(startsAt, endsAt);
        boolean wholeSlots = length.toSeconds() % SLOT.toSeconds() == 0 && length.toNanosPart() == 0;
        if (length.isNegative() || length.isZero() || !wholeSlots) {
            throw new InvalidVisitException("Visit length must be a positive multiple of 15 minutes");
        }
    }
}
