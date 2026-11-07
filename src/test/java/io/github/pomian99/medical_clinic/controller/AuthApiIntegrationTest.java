package io.github.pomian99.medical_clinic.controller;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtDecoder jwtDecoder;

    @Test
    void createToken_admin_returnsBearerTokenForOneHour() throws Exception {
        // when
        var result = mockMvc.perform(post("/auth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email": "admin", "password": "admin"}
                """));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(3600));
        String token = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.token");
        Jwt jwt = jwtDecoder.decode(token);
        assertEquals("medical-clinic", jwt.getClaimAsString(JwtClaimNames.ISS));
        assertEquals("admin", jwt.getSubject());
        assertEquals(List.of("ADMIN"), jwt.getClaimAsStringList("roles"));
        assertEquals(Duration.ofHours(1), Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt()));
    }

    @Test
    void createToken_wrongPassword_returnsUnauthorized() throws Exception {
        // when
        var result = mockMvc.perform(post("/auth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email": "admin", "password": "wrong-password"}
                """));

        // then
        result.andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.detail").value("wrong e-mail or password"));
    }

    @Test
    void getPatients_basicAuth_returnsUnauthorized() throws Exception {
        // when
        var result = mockMvc.perform(get("/patients").with(httpBasic("admin", "admin")));

        // then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    void getPatients_malformedBearerToken_returnsUnauthorized() throws Exception {
        // when
        var result = mockMvc.perform(get("/patients")
            .header(HttpHeaders.AUTHORIZATION, "Bearer abc"));

        // then
        result.andExpect(status().isUnauthorized());
    }
}
