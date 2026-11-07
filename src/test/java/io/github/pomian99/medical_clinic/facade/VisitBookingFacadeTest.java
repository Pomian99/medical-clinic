package io.github.pomian99.medical_clinic.facade;

import io.github.pomian99.medical_clinic.dto.BookingSummaryDto;
import io.github.pomian99.medical_clinic.exception.VisitAlreadyBookedException;
import io.github.pomian99.medical_clinic.mapper.DoctorMapper;
import io.github.pomian99.medical_clinic.mapper.PatientMapper;
import io.github.pomian99.medical_clinic.mapper.VisitMapper;
import io.github.pomian99.medical_clinic.model.Doctor;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.model.Visit;
import io.github.pomian99.medical_clinic.service.DoctorService;
import io.github.pomian99.medical_clinic.service.PatientService;
import io.github.pomian99.medical_clinic.service.VisitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitBookingFacadeTest {

    private static final LocalDate DAY = LocalDate.of(2030, 1, 15);

    @Mock
    VisitService visitService;

    @Mock
    PatientService patientService;

    @Mock
    DoctorService doctorService;

    VisitBookingFacade visitBookingFacade;

    @BeforeEach
    void setUp() {
        VisitMapper visitMapper = Mappers.getMapper(VisitMapper.class);
        PatientMapper patientMapper = Mappers.getMapper(PatientMapper.class);
        DoctorMapper doctorMapper = Mappers.getMapper(DoctorMapper.class);

        visitBookingFacade = new VisitBookingFacade(
            visitService, patientService, doctorService,
            visitMapper, patientMapper, doctorMapper
        );
    }

    @Test
    void book_visitFree_returnsBookingSummaryDto() {
        // given
        Patient patient = Patient.builder()
            .id(1L)
            .firstName("Jan")
            .build();
        Doctor doctor = new Doctor();
        doctor.setId(2L);
        doctor.setFirstName("Marek");
        Visit visit = Visit.builder()
            .id(3L)
            .doctor(doctor)
            .patient(patient)
            .startsAt(DAY.atTime(8, 0))
            .build();
        when(visitService.book(3L, 1L)).thenReturn(visit);
        when(patientService.findById(1L)).thenReturn(patient);
        when(doctorService.findById(2L)).thenReturn(doctor);
        when(visitService.countFreeSlots(2L, DAY)).thenReturn(5L);

        // when
        BookingSummaryDto summary = visitBookingFacade.book(3L, 1L);

        // then
        InOrder inOrder = inOrder(visitService);
        inOrder.verify(visitService).book(3L, 1L);
        inOrder.verify(visitService).countFreeSlots(2L, DAY);
        assertEquals(1L, summary.visit().patientId());
        assertEquals("Jan", summary.patient().firstName());
        assertEquals("Marek", summary.doctor().firstName());
        assertEquals(5L, summary.freeSlotsThatDay());
    }

    @Test
    void book_visitAlreadyBooked_throwsAndSkipsSummary() {
        // given
        VisitAlreadyBookedException alreadyBooked = new VisitAlreadyBookedException(3L);
        when(visitService.book(3L, 1L)).thenThrow(alreadyBooked);

        // when
        VisitAlreadyBookedException exception = assertThrows(
            VisitAlreadyBookedException.class,
            () -> visitBookingFacade.book(3L, 1L)
        );

        // then
        assertSame(alreadyBooked, exception);
        verifyNoInteractions(patientService, doctorService);
        verify(visitService, never()).countFreeSlots(any(), any());
    }
}
