package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.dto.BookingSummaryDto;
import io.github.pomian99.medical_clinic.dto.DoctorDto;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import io.github.pomian99.medical_clinic.dto.VisitDto;
import io.github.pomian99.medical_clinic.exception.PatientNotFoundException;
import io.github.pomian99.medical_clinic.exception.VisitAlreadyBookedException;
import io.github.pomian99.medical_clinic.facade.VisitBookingFacade;
import io.github.pomian99.medical_clinic.mapper.VisitMapperImpl;
import io.github.pomian99.medical_clinic.model.Specialization;
import io.github.pomian99.medical_clinic.service.VisitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VisitController.class)
@Import(VisitMapperImpl.class)
class VisitControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    VisitBookingFacade bookingFacade;

    @MockitoBean
    VisitService visitService;

    @Test
    void bookVisit_freeVisit_returnsSummaryFromFacade() throws Exception {
        // given
        LocalDateTime start = LocalDateTime.of(2030, 3, 4, 10, 0);
        BookingSummaryDto summary = new BookingSummaryDto(
            new VisitDto(1L, 1L, 2L, start, start.plusMinutes(30)),
            new PatientDto(2L, "jankow@poczta.pl", "Jan", "Kowalski", "123456789",
                LocalDate.of(1990, 5, 15), "Jan Kowalski"),
            new DoctorDto(1L, 4L, "katarzyna.wrobel@example.com", "Katarzyna", "Wróbel",
                Specialization.CARDIOLOGIST, List.of()),
            1L
        );
        when(bookingFacade.book(1L, 2L)).thenReturn(summary);

        // when
        var result = mockMvc.perform(patch("/visits/1/book/2"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.visit.patientId").value(2))
            .andExpect(jsonPath("$.patient.fullName").value("Jan Kowalski"))
            .andExpect(jsonPath("$.doctor.lastName").value("Wróbel"))
            .andExpect(jsonPath("$.freeSlotsThatDay").value(1));
        verifyNoInteractions(visitService);
    }

    @Test
    void bookVisit_visitAlreadyBooked_returnsConflict() throws Exception {
        // given
        when(bookingFacade.book(1L, 3L)).thenThrow(new VisitAlreadyBookedException(1L));

        // when
        var result = mockMvc.perform(patch("/visits/1/book/3"));

        // then
        result.andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Visit with id 1 is already booked"));
    }

    @Test
    void bookVisit_patientNotFound_returnsNotFound() throws Exception {
        // given
        when(bookingFacade.book(2L, 99L)).thenThrow(new PatientNotFoundException(99L));

        // when
        var result = mockMvc.perform(patch("/visits/2/book/99"));

        // then
        result.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Patient with id 99 not found"));
    }
}
