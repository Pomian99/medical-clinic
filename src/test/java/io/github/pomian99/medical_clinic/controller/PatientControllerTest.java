package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.mapper.PatientMapperImpl;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@Import(PatientMapperImpl.class)
class PatientControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PatientService patientService;

    @Test
    void getPatients_twoPatientsExist_returnsBoth() throws Exception {
        // given
        Patient jan = Patient.builder()
            .id(1L)
            .firstName("Jan")
            .build();
        Patient anna = Patient.builder()
            .id(2L)
            .firstName("Anna")
            .build();
        when(patientService.findAll()).thenReturn(List.of(jan, anna));

        // when
        var result = mockMvc.perform(get("/patients"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[1].firstName").value("Anna"));
    }

    @Test
    void getPatientById_patientExists_returnsPatientWithoutIdCardNo() throws Exception {
        // given
        Patient patient = Patient.builder()
            .id(1L)
            .idCardNo("ABC123456")
            .firstName("Jan")
            .lastName("Kowalski")
            .user(
                User.builder()
                    .email("jankow@poczta.pl")
                    .build()
            )
            .build();
        when(patientService.findById(1L)).thenReturn(patient);

        // when
        var result = mockMvc.perform(get("/patients/1"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("jankow@poczta.pl"))
            .andExpect(jsonPath("$.firstName").value("Jan"))
            .andExpect(jsonPath("$.lastName").value("Kowalski"))
            .andExpect(jsonPath("$.idCardNo").doesNotExist());
    }

    @Test
    void createPatient_validBody_returnsCreated() throws Exception {
        // given
        String body = """
            {
                "email": "jankow@poczta.pl",
                "password": "Secret123!",
                "idCardNo": "ABC123456",
                "firstName": "Jan",
                "lastName": "Kowalski",
                "phoneNumber": "123456789",
                "birthday": "1990-05-15"
            }
            """;
        Patient patient = Patient.builder()
            .id(1L)
            .firstName("Jan")
            .lastName("Kowalski")
            .user(
                User.builder()
                    .email("jankow@poczta.pl")
                    .build()
            )
            .build();
        when(patientService.create(any())).thenReturn(patient);

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("jankow@poczta.pl"));
    }
}
