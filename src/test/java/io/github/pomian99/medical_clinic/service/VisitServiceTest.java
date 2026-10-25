package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.VisitCreateCommand;
import io.github.pomian99.medical_clinic.exception.InvalidVisitException;
import io.github.pomian99.medical_clinic.mapper.VisitMapper;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.model.Visit;
import io.github.pomian99.medical_clinic.repository.DoctorRepository;
import io.github.pomian99.medical_clinic.repository.PatientRepository;
import io.github.pomian99.medical_clinic.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
