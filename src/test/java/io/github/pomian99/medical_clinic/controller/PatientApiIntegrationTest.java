package io.github.pomian99.medical_clinic.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
    void createPatient_thenGetByEmail_forbiddenForPatientAdminFindsNewPatient() throws Exception {
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
        String patientToken = token("anna.nowak@poczta.pl", "Secret123!");
        String adminToken = token("admin", "admin");

        // when
        var asPatient = mockMvc.perform(get("/patients")
            .param("email", "anna.nowak@poczta.pl")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + patientToken));
        var asAdmin = mockMvc.perform(get("/patients")
            .param("email", "anna.nowak@poczta.pl")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken));

        // then
        asPatient.andExpect(status().isForbidden());
        asAdmin.andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("anna.nowak@poczta.pl"))
            .andExpect(jsonPath("$.firstName").value("Anna"))
            .andExpect(jsonPath("$.lastName").value("Nowak"));
    }

    private String token(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email": "%s", "password": "%s"}
                    """.formatted(email, password)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }
}
