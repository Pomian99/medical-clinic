package io.github.pomian99.medical_clinic.facade;

import io.github.pomian99.medical_clinic.dto.BookingSummaryDto;
import io.github.pomian99.medical_clinic.dto.DoctorDto;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import io.github.pomian99.medical_clinic.dto.VisitDto;
import io.github.pomian99.medical_clinic.exception.VisitAlreadyBookedException;
import io.github.pomian99.medical_clinic.service.DoctorService;
import io.github.pomian99.medical_clinic.service.PatientService;
import io.github.pomian99.medical_clinic.service.VisitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

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

    @InjectMocks
    VisitBookingFacade visitBookingFacade;

    @Test
    void book_visitFree_returnsBookingSummaryDto() {
        // given
        when(visitService.book(3L, 1L)).thenReturn(
            new VisitDto(3L, 2L, 1L, DAY.atTime(8, 0), DAY.atTime(8, 30))
        );
        when(patientService.findById(1L)).thenReturn(
            new PatientDto(1L, "jankow@poczta.pl", "Jan", "Kowalski", null, null, "Jan Kowalski")
        );
        when(doctorService.findById(2L)).thenReturn(
            new DoctorDto(2L, null, null, "Marek", null, null, List.of())
        );
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
