package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.config.SecurityConfig;
import io.github.pomian99.medical_clinic.dto.DoctorDto;
import io.github.pomian99.medical_clinic.dto.VisitDto;
import io.github.pomian99.medical_clinic.model.Specialization;
import io.github.pomian99.medical_clinic.service.DoctorService;
import io.github.pomian99.medical_clinic.service.VisitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorController.class)
@Import(SecurityConfig.class)
class DoctorControllerTest {

    private static final String SUBSTITUTION = """
        {"substituteId": 2}
        """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DoctorService doctorService;

    @MockitoBean
    VisitService visitService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void substitute_admin_returnsMovedVisits() throws Exception {
        // given
        LocalDateTime start = LocalDateTime.of(2030, 2, 11, 10, 0);
        when(visitService.substitute(1L, 2L)).thenReturn(List.of(
            new VisitDto(5L, 2L, null, start, start.plusMinutes(30))
        ));

        // when
        var result = mockMvc.perform(post("/doctors/1/substitutions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(SUBSTITUTION));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(5))
            .andExpect(jsonPath("$[0].doctorId").value(2));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void substitute_patient_returnsForbidden() throws Exception {
        // when
        var result = mockMvc.perform(post("/doctors/1/substitutions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(SUBSTITUTION));

        // then
        result.andExpect(status().isForbidden());
        verifyNoInteractions(visitService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteDoctor_admin_returnsNoContent() throws Exception {
        // when
        var result = mockMvc.perform(delete("/doctors/1"));

        // then
        result.andExpect(status().isNoContent());
        verify(doctorService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void deleteDoctor_patient_returnsForbidden() throws Exception {
        // when
        var result = mockMvc.perform(delete("/doctors/1"));

        // then
        result.andExpect(status().isForbidden());
        verifyNoInteractions(doctorService);
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    void removeFacility_doctor_stillAllowedNextToAdminOnlyDelete() throws Exception {
        // given
        when(doctorService.removeFacility(1L, 2L)).thenReturn(
            new DoctorDto(1L, 4L, "katarzyna.wrobel@example.com", "Katarzyna", "Wróbel",
                Specialization.CARDIOLOGIST, List.of())
        );

        // when
        var result = mockMvc.perform(delete("/doctors/1/facilities/2"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.facilities").isEmpty());
    }
}
