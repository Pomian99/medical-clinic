package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostmanController.class)
@Import(SecurityConfig.class)
class PostmanControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void patientsCollection_admin_returnsPostmanCollection() throws Exception {
        // when
        var result = mockMvc.perform(get("/postman/patients"));

        // then
        result.andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.info.name").value("Medical Clinic"));
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void patientsCollection_patient_returnsForbidden() throws Exception {
        // when
        var result = mockMvc.perform(get("/postman/patients"));

        // then
        result.andExpect(status().isForbidden());
    }
}
