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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    // "Now" for every test: 15.01.2030 8:00.
    static final LocalDateTime NOW = LocalDateTime.of(2030, 1, 15, 8, 0);

    @Mock
    VisitRepository visitRepository;

    @Mock
    DoctorRepository doctorRepository;

    @Mock
    PatientRepository patientRepository;

    VisitService visitService;

    @BeforeEach
    void setUp() {
        VisitMapper mapper = Mappers.getMapper(VisitMapper.class);
        Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        visitService = new VisitService(visitRepository, doctorRepository, patientRepository, mapper, clock);
    }

    @Test
    void create_startsExactlyNow_savesVisitWithDoctor() {
        // given
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        VisitCreateCommand command = new VisitCreateCommand(
            1L,
            NOW,
            NOW.plusMinutes(30)
        );
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(1L, NOW.plusMinutes(30), NOW))
            .thenReturn(false);
        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);

        // when
        visitService.create(command);

        // then
        verify(visitRepository).save(captor.capture());
        Visit saved = captor.getValue();
        assertEquals(NOW, saved.getStartsAt());
        assertEquals(NOW.plusMinutes(30), saved.getEndsAt());
        assertSame(doctor, saved.getDoctor());
    }

    @Test
    void create_startsBeforeNow_throwsInvalidVisit() {
        // given
        VisitCreateCommand command = new VisitCreateCommand(
            1L,
            NOW.minusMinutes(15),
            NOW.plusMinutes(15)
        );

        // when
        InvalidVisitException exception = assertThrows(
            InvalidVisitException.class,
            () -> visitService.create(command)
        );

        // then
        assertEquals("Visit start must not be in the past", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void create_startsBetweenQuarters_throwsInvalidVisit() {
        // given
        VisitCreateCommand command = new VisitCreateCommand(
            1L,
            NOW.plusMinutes(10),
            NOW.plusMinutes(40)
        );

        // when
        InvalidVisitException exception = assertThrows(
            InvalidVisitException.class,
            () -> visitService.create(command)
        );

        // then
        assertEquals("Visit must start at :00, :15, :30 or :45 with zero seconds", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void create_lengthNotMultipleOfQuarter_throwsInvalidVisit() {
        // given
        VisitCreateCommand command = new VisitCreateCommand(
            1L,
            NOW.plusHours(1),
            NOW.plusHours(1).plusMinutes(20)
        );

        // when
        InvalidVisitException exception = assertThrows(
            InvalidVisitException.class,
            () -> visitService.create(command)
        );

        // then
        assertEquals("Visit length must be a positive multiple of 15 minutes", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void create_doctorNotFound_throwsDoctorNotFound() {
        // given
        VisitCreateCommand command = new VisitCreateCommand(
            1L,
            NOW.plusHours(1),
            NOW.plusHours(1).plusMinutes(30)
        );
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        DoctorNotFoundException exception = assertThrows(
            DoctorNotFoundException.class,
            () -> visitService.create(command)
        );

        // then
        assertEquals("Doctor with id 1 not found", exception.getMessage());
        verify(visitRepository, never()).save(any());
    }

    @Test
    void create_overlapsOtherVisit_throwsVisitOverlaps() {
        // given
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        LocalDateTime startsAt = NOW.plusHours(1);
        LocalDateTime endsAt = startsAt.plusMinutes(30);
        VisitCreateCommand command = new VisitCreateCommand(1L, startsAt, endsAt);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(visitRepository.existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(1L, endsAt, startsAt))
            .thenReturn(true);

        // when
        VisitOverlapsException exception = assertThrows(
            VisitOverlapsException.class,
            () -> visitService.create(command)
        );

        // then
        assertEquals(
            "Doctor with id 1 already has a visit overlapping 2030-01-15T09:00 - 2030-01-15T09:30",
            exception.getMessage()
        );
        verify(visitRepository, never()).save(any());
    }

    @Test
    void book_freeVisit_assignsPatient() {
        // given
        Visit visit = Visit.builder()
            .id(1L)
            .startsAt(NOW.plusDays(1))
            .endsAt(NOW.plusDays(1).plusMinutes(30))
            .build();
        Patient patient = Patient.builder()
            .id(2L)
            .build();
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

        // when
        Visit result = visitService.book(1L, 2L);

        // then
        assertSame(patient, result.getPatient());
    }

    @Test
    void book_visitNotFound_throwsVisitNotFound() {
        // given
        when(visitRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        VisitNotFoundException exception = assertThrows(
            VisitNotFoundException.class,
            () -> visitService.book(1L, 2L)
        );

        // then
        assertEquals("Visit with id 1 not found", exception.getMessage());
    }

    @Test
    void book_patientNotFound_throwsPatientNotFound() {
        // given
        Visit visit = Visit.builder()
            .id(1L)
            .startsAt(NOW.plusDays(1))
            .endsAt(NOW.plusDays(1).plusMinutes(30))
            .build();
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(patientRepository.findById(2L)).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(
            PatientNotFoundException.class,
            () -> visitService.book(1L, 2L)
        );

        // then
        assertEquals("Patient with id 2 not found", exception.getMessage());
    }

    @Test
    void book_visitYesterday_throwsInvalidVisit() {
        // given
        Visit visit = Visit.builder()
            .id(1L)
            .startsAt(NOW.minusDays(1))
            .endsAt(NOW.minusDays(1).plusMinutes(30))
            .build();
        Patient patient = Patient.builder()
            .id(2L)
            .build();
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

        // when
        InvalidVisitException exception = assertThrows(
            InvalidVisitException.class,
            () -> visitService.book(1L, 2L)
        );

        // then
        assertEquals("Visit with id 1 is in the past and cannot be booked", exception.getMessage());
    }

    @Test
    void book_visitAlreadyBooked_throwsVisitAlreadyBooked() {
        // given
        Patient bookedPatient = Patient.builder()
            .id(3L)
            .build();
        Visit visit = Visit.builder()
            .id(1L)
            .startsAt(NOW.plusDays(1))
            .endsAt(NOW.plusDays(1).plusMinutes(30))
            .patient(bookedPatient)
            .build();
        Patient patient = Patient.builder()
            .id(2L)
            .build();
        when(visitRepository.findById(1L)).thenReturn(Optional.of(visit));
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));

        // when
        VisitAlreadyBookedException exception = assertThrows(
            VisitAlreadyBookedException.class,
            () -> visitService.book(1L, 2L)
        );

        // then
        assertEquals("Visit with id 1 is already booked", exception.getMessage());
        assertSame(bookedPatient, visit.getPatient());
    }
}
