package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.config.SecurityConfig;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import io.github.pomian99.medical_clinic.exception.PatientAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.PatientHasVisitsException;
import io.github.pomian99.medical_clinic.exception.PatientNotFoundException;
import io.github.pomian99.medical_clinic.exception.UserAlreadyExistsException;
import io.github.pomian99.medical_clinic.service.PatientService;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@Import(SecurityConfig.class)
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

    static final PatientDto JAN = new PatientDto(
        1L,
        "jankow@poczta.pl",
        "Jan",
        "Kowalski",
        "123456789",
        LocalDate.of(1990, 5, 15),
        "Jan Kowalski"
    );

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PatientService patientService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPatients_twoPatientsExist_returnsBoth() throws Exception {
        // given
        PatientDto anna = new PatientDto(
            2L,
            "anna.nowak@example.com",
            "Anna",
            "Nowak",
            "987654321",
            LocalDate.of(1985, 3, 2),
            "Anna Nowak"
        );
        when(patientService.findAll()).thenReturn(List.of(JAN, anna));

        // when
        var result = mockMvc.perform(get("/patients"));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[1].firstName").value("Anna"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void getPatientById_patientExists_returnsPatientWithoutIdCardNo() throws Exception {
        // given
        when(patientService.findById(1L)).thenReturn(JAN);

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
    @WithMockUser(roles = "PATIENT")
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
    @WithMockUser(roles = "PATIENT")
    void getPatientById_idNotANumber_returnsBadRequest() throws Exception {
        // when
        var result = mockMvc.perform(get("/patients/abc"));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Path variable id has a wrong type"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
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
    @WithMockUser(roles = "PATIENT")
    void getPatientById_xmlRequested_returnsNotAcceptable() throws Exception {
        // given
        when(patientService.findById(1L)).thenReturn(JAN);

        // when
        var result = mockMvc.perform(get("/patients/1")
            .accept(MediaType.APPLICATION_XML));

        // then
        result.andExpect(status().isNotAcceptable());
    }

    @Test
    void createPatient_validBody_returnsCreated() throws Exception {
        // given
        when(patientService.create(any())).thenReturn(JAN);

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
    void createPatient_noBirthday_returnsBadRequest() throws Exception {
        // given
        String body = """
            {
                "email": "jankow@poczta.pl",
                "password": "Secret123!",
                "idCardNo": "ABC123456",
                "firstName": "Jan",
                "lastName": "Kowalski",
                "phoneNumber": "123456789"
            }
            """;

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].field", hasItem("birthday")))
            .andExpect(jsonPath("$.errors[*].message", hasItem("Birthday is required")));
        verify(patientService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void updatePatient_noBirthday_returnsBadRequestAndKeepsData() throws Exception {
        // given
        String body = """
            {
                "email": "jankow@poczta.pl",
                "firstName": "Janusz",
                "lastName": "Nowak",
                "phoneNumber": "987654321"
            }
            """;

        // when
        var result = mockMvc.perform(put("/patients/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].field", hasItem("birthday")))
            .andExpect(jsonPath("$.errors[*].message", hasItem("Birthday is required")));
        verify(patientService, never()).update(anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void updatePatient_emailOfOtherAccount_returnsConflict() throws Exception {
        // given
        String body = """
            {
                "email": "anna.nowak@example.com",
                "firstName": "Janusz",
                "lastName": "Nowak",
                "phoneNumber": "987654321",
                "birthday": "1990-05-15"
            }
            """;
        when(patientService.update(anyLong(), any())).thenThrow(new UserAlreadyExistsException());

        // when
        var result = mockMvc.perform(put("/patients/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("User with this email already exists"));
    }

    @Test
    void createPatient_emailTaken_returnsConflict() throws Exception {
        // given
        when(patientService.create(any())).thenThrow(new UserAlreadyExistsException());

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_BODY));

        // then
        result.andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("User with this email already exists"));
    }

    @Test
    void createPatient_idCardNoTaken_returnsConflictWithoutNumber() throws Exception {
        // given
        when(patientService.create(any())).thenThrow(new PatientAlreadyExistsException());

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_BODY));

        // then
        result.andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Patient with this ID card number already exists"))
            .andExpect(content().string(not(containsString("ABC123456"))));
    }

    @Test
    void createPatient_idCardNoTakenInDatabase_returnsConflictWithoutValue() throws Exception {
        // given
        when(patientService.create(any())).thenThrow(constraintViolation(ConstraintKind.UNIQUE));

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_BODY));

        // then
        result.andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("The request conflicts with data already stored"))
            .andExpect(content().string(not(containsString("id_card_no"))))
            .andExpect(content().string(not(containsString("ABC123456"))));
    }

    @Test
    void createPatient_notNullViolatedInDatabase_returnsInternalError() throws Exception {
        // given
        when(patientService.create(any())).thenThrow(constraintViolation(ConstraintKind.NOT_NULL));

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_BODY));

        // then
        result.andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.detail").value("Unknown error"));
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
    void createPatient_birthdayInWrongFormat_returnsFieldError() throws Exception {
        // given
        String body = """
            {
                "email": "jankow@poczta.pl",
                "password": "Secret123!",
                "idCardNo": "ABC123456",
                "firstName": "Jan",
                "lastName": "Kowalski",
                "phoneNumber": "123456789",
                "birthday": "31-12-1990"
            }
            """;

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Request body has a field with a wrong type or format"))
            .andExpect(jsonPath("$.errors[0].field").value("birthday"))
            .andExpect(content().string(not(containsString("31-12-1990"))));
        verify(patientService, never()).create(any());
    }

    @Test
    void createPatient_plainTextBody_returnsUnsupportedMediaType() throws Exception {
        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.TEXT_PLAIN)
            .content(VALID_BODY));

        // then
        result.andExpect(status().isUnsupportedMediaType())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        verify(patientService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void deletePatient_patientExists_returnsNoContent() throws Exception {
        // when
        var result = mockMvc.perform(delete("/patients/1"));

        // then
        result.andExpect(status().isNoContent());
        verify(patientService).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "PATIENT")
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
    @WithMockUser(roles = "PATIENT")
    void deletePatient_patientHasVisits_returnsConflict() throws Exception {
        // given
        doThrow(new PatientHasVisitsException(1L)).when(patientService).deleteById(1L);

        // when
        var result = mockMvc.perform(delete("/patients/1"));

        // then
        result.andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Patient with id 1 has visits and cannot be deleted"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void deletePatients_noId_returnsMethodNotAllowed() throws Exception {
        // when
        var result = mockMvc.perform(delete("/patients"));

        // then
        result.andExpect(status().isMethodNotAllowed())
            .andExpect(jsonPath("$.detail").value("Method DELETE is not supported here"));
    }

    private static DataIntegrityViolationException constraintViolation(ConstraintKind kind) {
        SQLException sqlException = new SQLException(
            "ERROR: duplicate key value violates unique constraint \"uk3usu2gl5shfee2u3f4gfw4w3p\""
                + " Detail: Key (id_card_no)=(ABC123456) already exists."
        );
        ConstraintViolationException violation = new ConstraintViolationException(
            "could not execute statement",
            sqlException,
            "insert into patients (id_card_no) values (?)",
            kind,
            "uk3usu2gl5shfee2u3f4gfw4w3p"
        );
        return new DataIntegrityViolationException(violation.getMessage(), violation);
    }
}
