package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.exception.PatientNotFoundException;
import io.github.pomian99.medical_clinic.exception.UserAlreadyExistsException;
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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@Import(PatientMapperImpl.class)
class PatientControllerTest {

    // Meets every rule of PatientCreateCommand.
    static final String VALID_BODY = """
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
            .andExpect(jsonPath("$.fullName").value("Jan Kowalski"))
            .andExpect(jsonPath("$.idCardNo").doesNotExist());
    }

    @Test
    void getPatientById_patientNotFound_returnsNotFound() throws Exception {
        // given
        when(patientService.findById(1L)).thenThrow(new PatientNotFoundException(1L));

        // when
        var result = mockMvc.perform(get("/patients/1"));

        // then
        result.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Patient with id 1 not found"));
    }

    @Test
    void getPatientById_idNotANumber_returnsBadRequest() throws Exception {
        // when
        var result = mockMvc.perform(get("/patients/abc"));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Path variable id has a wrong type"));
    }

    @Test
    void getPatientById_unexpectedError_returnsInternalErrorWithoutDetails() throws Exception {
        // given
        when(patientService.findById(1L)).thenThrow(new IllegalStateException("Connection pool exhausted"));

        // when
        var result = mockMvc.perform(get("/patients/1"));

        // then
        result.andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.detail").value("Unknown error"))
            .andExpect(content().string(not(containsString("Connection pool exhausted"))));
    }

    @Test
    void createPatient_validBody_returnsCreated() throws Exception {
        // given
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
            .content(VALID_BODY));

        // then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("jankow@poczta.pl"));
    }

    @Test
    void createPatient_blankFirstName_returnsBadRequest() throws Exception {
        // given
        String body = """
            {
                "email": "jankow@poczta.pl",
                "password": "Secret123!",
                "idCardNo": "ABC123456",
                "firstName": "",
                "lastName": "Kowalski",
                "phoneNumber": "123456789",
                "birthday": "1990-05-15"
            }
            """;

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].field", hasItem("firstName")))
            .andExpect(jsonPath("$.errors[*].message", hasItem("First name cannot be blank")));
        verify(patientService, never()).create(any());
    }

    @Test
    void createPatient_emailTaken_returnsConflict() throws Exception {
        // given
        when(patientService.create(any())).thenThrow(new UserAlreadyExistsException("jankow@poczta.pl"));

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_BODY));

        // then
        result.andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("User with email jankow@poczta.pl already exists"));
    }

    @Test
    void createPatient_invalidJson_returnsBadRequest() throws Exception {
        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{ \"email\": "));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Request body is not valid JSON"));
        verify(patientService, never()).create(any());
    }

    @Test
    void deletePatient_patientExists_returnsNoContent() throws Exception {
        // when
        var result = mockMvc.perform(delete("/patients/1"));

        // then
        result.andExpect(status().isNoContent());
        verify(patientService).deleteById(1L);
    }

    @Test
    void deletePatient_patientNotFound_returnsNotFound() throws Exception {
        // given
        doThrow(new PatientNotFoundException(1L)).when(patientService).deleteById(1L);

        // when
        var result = mockMvc.perform(delete("/patients/1"));

        // then
        result.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Patient with id 1 not found"));
    }

    @Test
    void deletePatients_noId_returnsMethodNotAllowed() throws Exception {
        // when
        var result = mockMvc.perform(delete("/patients"));

        // then
        result.andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.detail").value("Method DELETE is not supported here"));
    }
}
