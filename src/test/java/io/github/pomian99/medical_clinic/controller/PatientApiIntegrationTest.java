package io.github.pomian99.medical_clinic.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PatientApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void createPatient_thenGetByEmail_findsNewPatient() throws Exception {
        // given
        String body = """
            {
                "email": "anna.nowak@poczta.pl",
                "password": "Secret123!",
                "idCardNo": "XYZ654321",
                "firstName": "Anna",
                "lastName": "Nowak",
                "phoneNumber": "987654321",
                "birthday": "1985-03-20"
            }
            """;
        mockMvc.perform(post("/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());

        // when
        String credentials = Base64.getEncoder().encodeToString(
            "anna.nowak@poczta.pl:Secret123!".getBytes(StandardCharsets.UTF_8));
        var result = mockMvc.perform(get("/patients")
            .param("email", "anna.nowak@poczta.pl")
            .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("anna.nowak@poczta.pl"))
            .andExpect(jsonPath("$.firstName").value("Anna"))
            .andExpect(jsonPath("$.lastName").value("Nowak"));
    }
}
