package io.github.pomian99.medical_clinic.controller;

import com.jayway.jsonpath.JsonPath;
import io.github.pomian99.medical_clinic.model.Doctor;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.model.Role;
import io.github.pomian99.medical_clinic.model.Specialization;
import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.repository.DoctorRepository;
import io.github.pomian99.medical_clinic.repository.PatientRepository;
import io.github.pomian99.medical_clinic.repository.UserRepository;
import io.github.pomian99.medical_clinic.service.PatientService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PatientApiIntegrationTest {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private static final String PASSWORD = "Secret123!";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ApplicationContext context;

    @Autowired
    PatientRepository patientRepository;

    @Autowired
    PatientService patientService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DoctorRepository doctorRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TransactionTemplate transactionTemplate;

    private final List<Long> registeredIds = new ArrayList<>();

    private SharedAccount sharedAccount;

    @AfterEach
    void deleteRegisteredPatients() {
        for (Long id : registeredIds) {
            if (patientRepository.existsById(id)) {
                patientService.deleteById(id);
            }
        }
        if (sharedAccount != null) {
            if (patientRepository.existsById(sharedAccount.patientId())) {
                patientRepository.deleteById(sharedAccount.patientId());
            }
            doctorRepository.deleteById(sharedAccount.doctorId());
            userRepository.deleteById(sharedAccount.userId());
        }
    }

    @Test
    void context_patientControllerIsRegistered() {
        // when
        PatientController controller = context.getBean(PatientController.class);

        // then
        assertNotNull(controller);
    }

    @Test
    void createPatient_validBody_returnsCreatedPatientWithoutSecrets() throws Exception {
        // given
        String body = patientBody("Zofia.Created@Example.com", "CRE123456", "Zofia", "Kwiatek", "1990-05-15");

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("zofia.created@example.com"))
            .andExpect(jsonPath("$.fullName").value("Zofia Kwiatek"))
            .andExpect(jsonPath("$.idCardNo").doesNotExist())
            .andExpect(jsonPath("$.password").doesNotExist());
        registeredIds.add(idOf(result.andReturn().getResponse().getContentAsString()));
    }

    @Test
    void getPatientById_registeredPatient_returnsPatient() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");

        // when
        var result = mockMvc.perform(get("/patients/{id}", jan.id())
            .header(HttpHeaders.AUTHORIZATION, bearer(jan)));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(jan.id()))
            .andExpect(jsonPath("$.email").value(jan.email()))
            .andExpect(jsonPath("$.birthday").value("1990-05-15"));
    }

    @Test
    void getPatients_asAdmin_listContainsRegisteredPatient() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");

        // when
        var result = mockMvc.perform(get("/patients")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("admin", "admin")));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$[*].id", hasItem((int) jan.id())));
    }

    @Test
    void createPatient_thenGetByEmail_forbiddenForPatientAdminFindsNewPatient() throws Exception {
        // given
        Registered anna = register("Anna", "Nowak");
        String adminToken = token("admin", "admin");

        // when
        var asPatient = mockMvc.perform(get("/patients")
            .param("email", anna.email())
            .header(HttpHeaders.AUTHORIZATION, bearer(anna)));
        var asAdmin = mockMvc.perform(get("/patients")
            .param("email", anna.email())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken));

        // then
        asPatient.andExpect(status().isForbidden());
        asAdmin.andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(anna.email()))
            .andExpect(jsonPath("$.firstName").value("Anna"))
            .andExpect(jsonPath("$.lastName").value("Nowak"));
    }

    @Test
    void getPatientByEmail_otherLetterCase_findsPatient() throws Exception {
        // given
        Registered anna = register("Anna", "Nowak");

        // when
        var result = mockMvc.perform(get("/patients")
            .param("email", anna.email().toUpperCase())
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("admin", "admin")));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(anna.id()));
    }

    @Test
    void getPatientsByFragment_asAdmin_returnsMatchesSortedByLastAndFirstName() throws Exception {
        // given
        register("Zofia", "Kwiatkowska");
        register("Bogdan", "Kwiatek");
        register("Adam", "Kwiatek");

        // when
        var result = mockMvc.perform(get("/patients")
            .param("fragment", "KWIAT")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("admin", "admin")));

        // then
        result.andExpect(status().isOk());
        List<String> names = JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$[*].fullName");
        assertEquals(List.of("Adam Kwiatek", "Bogdan Kwiatek", "Zofia Kwiatkowska"), names);
    }

    @Test
    void getPatientsByFragment_percentSign_searchesForItLiterally() throws Exception {
        // given
        register("Jan", "Kowalski");

        // when
        var result = mockMvc.perform(get("/patients")
            .param("fragment", "%")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token("admin", "admin")));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updatePatient_validBody_changesDataAndKeepsIdCardNo() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");
        String body = updateBody("Janusz.Nowy@Example.com", "Janusz", "Nowak", "1991-06-16");

        // when
        var result = mockMvc.perform(put("/patients/{id}", jan.id())
            .header(HttpHeaders.AUTHORIZATION, bearer(jan))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isOk())
            .andExpect(jsonPath("$.fullName").value("Janusz Nowak"))
            .andExpect(jsonPath("$.email").value("janusz.nowy@example.com"))
            .andExpect(jsonPath("$.birthday").value("1991-06-16"));
        assertEquals(jan.idCardNo(), patientRepository.findById(jan.id()).orElseThrow().getIdCardNo());
    }

    @Test
    void updatePatient_blankFirstName_returnsBadRequestAndKeepsData() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");
        String body = updateBody(jan.email(), "", "Nowak", "1991-06-16");

        // when
        var result = mockMvc.perform(put("/patients/{id}", jan.id())
            .header(HttpHeaders.AUTHORIZATION, bearer(jan))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].field", hasItem("firstName")));
        mockMvc.perform(get("/patients/{id}", jan.id()).header(HttpHeaders.AUTHORIZATION, bearer(jan)))
            .andExpect(jsonPath("$.fullName").value("Jan Kowalski"))
            .andExpect(jsonPath("$.birthday").value("1990-05-15"));
    }

    @Test
    void updatePatient_noBirthday_returnsBadRequestAndKeepsBirthday() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");
        String body = """
            {
                "email": "%s",
                "firstName": "Janusz",
                "lastName": "Nowak",
                "phoneNumber": "987654321"
            }
            """.formatted(jan.email());

        // when
        var result = mockMvc.perform(put("/patients/{id}", jan.id())
            .header(HttpHeaders.AUTHORIZATION, bearer(jan))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].field", hasItem("birthday")));
        mockMvc.perform(get("/patients/{id}", jan.id()).header(HttpHeaders.AUTHORIZATION, bearer(jan)))
            .andExpect(jsonPath("$.birthday").value("1990-05-15"))
            .andExpect(jsonPath("$.firstName").value("Jan"));
    }

    @Test
    void updatePatientPassword_newPassword_onlyNewPasswordLogsIn() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");

        // when
        var result = mockMvc.perform(patch("/patients/{id}/password", jan.id())
            .header(HttpHeaders.AUTHORIZATION, bearer(jan))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"password": "NewSecret123!"}
                """));

        // then
        result.andExpect(status().isOk());
        assertEquals(401, loginStatus(jan.email(), PASSWORD));
        assertEquals(200, loginStatus(jan.email(), "NewSecret123!"));
    }

    @Test
    void deletePatient_withoutVisits_removesPatientAndAccount() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");
        String janToken = bearer(jan);

        // when
        var result = mockMvc.perform(delete("/patients/{id}", jan.id())
            .header(HttpHeaders.AUTHORIZATION, janToken));

        // then
        result.andExpect(status().isNoContent());
        assertFalse(patientRepository.existsById(jan.id()));
        assertEquals(401, loginStatus(jan.email(), PASSWORD));
        mockMvc.perform(get("/patients/{id}", jan.id()).header(HttpHeaders.AUTHORIZATION, janToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void deletePatient_accountAlsoBelongsToDoctor_removesPatientKeepsAccountAndDoctor() throws Exception {
        // given
        sharedAccount = doctorWhoIsAlsoPatient();
        String janToken = bearer(register("Jan", "Kowalski"));

        // when
        var result = mockMvc.perform(delete("/patients/{id}", sharedAccount.patientId())
            .header(HttpHeaders.AUTHORIZATION, janToken));

        // then
        result.andExpect(status().isNoContent());
        assertFalse(patientRepository.existsById(sharedAccount.patientId()));
        assertTrue(doctorRepository.existsById(sharedAccount.doctorId()));
        assertEquals(200, loginStatus(sharedAccount.email(), PASSWORD));
    }

    @Test
    void getPatientById_unknownId_returnsNotFound() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");

        // when
        var result = mockMvc.perform(get("/patients/{id}", 999999)
            .header(HttpHeaders.AUTHORIZATION, bearer(jan)));

        // then
        result.andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").value("Patient with id 999999 not found"));
    }

    @Test
    void createPatient_emailTakenInOtherLetterCase_returnsConflict() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");
        String body = patientBody(jan.email().toUpperCase(), nextIdCardNo(), "Jan", "Kowalski", "1990-05-15");

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("User with this email already exists"));
    }

    @Test
    void createPatient_idCardNoTaken_returnsConflictWithoutNumber() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");
        String body = patientBody(nextEmail(), jan.idCardNo(), "Jan", "Kowalski", "1990-05-15");

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value("Patient with this ID card number already exists"))
            .andExpect(content().string(not(containsString(jan.idCardNo()))));
    }

    @Test
    void createPatient_blankFirstName_returnsBadRequestWithFieldAndSavesNothing() throws Exception {
        // given
        String idCardNo = nextIdCardNo();
        String body = patientBody(nextEmail(), idCardNo, "", "Kowalski", "1990-05-15");

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].field", hasItem("firstName")))
            .andExpect(jsonPath("$.errors[*].message", hasItem("First name cannot be blank")));
        assertFalse(patientRepository.existsByIdCardNo(idCardNo));
    }

    @Test
    void createPatient_noBirthday_returnsBadRequestWithField() throws Exception {
        // given
        String body = """
            {
                "email": "%s",
                "password": "%s",
                "idCardNo": "%s",
                "firstName": "Jan",
                "lastName": "Kowalski",
                "phoneNumber": "123456789"
            }
            """.formatted(nextEmail(), PASSWORD, nextIdCardNo());

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[*].field", hasItem("birthday")));
    }

    @Test
    void createPatient_birthdayInWrongFormat_returnsBadRequestWithField() throws Exception {
        // given
        String body = patientBody(nextEmail(), nextIdCardNo(), "Jan", "Kowalski", "15-05-1990");

        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors[0].field").value("birthday"))
            .andExpect(content().string(not(containsString("15-05-1990"))));
    }

    @Test
    void createPatient_unreadableJson_returnsBadRequest() throws Exception {
        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{ \"email\": "));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Request body is not valid JSON"));
    }

    @Test
    void createPatient_plainTextBody_returnsUnsupportedMediaType() throws Exception {
        // when
        var result = mockMvc.perform(post("/patients")
            .contentType(MediaType.TEXT_PLAIN)
            .content(patientBody(nextEmail(), nextIdCardNo(), "Jan", "Kowalski", "1990-05-15")));

        // then
        result.andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void getPatientById_idNotANumber_returnsBadRequest() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");

        // when
        var result = mockMvc.perform(get("/patients/abc")
            .header(HttpHeaders.AUTHORIZATION, bearer(jan)));

        // then
        result.andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.detail").value("Path variable id has a wrong type"));
    }

    @Test
    void deletePatients_noId_returnsMethodNotAllowedWithAllowHeader() throws Exception {
        // given
        Registered jan = register("Jan", "Kowalski");

        // when
        var result = mockMvc.perform(delete("/patients")
            .header(HttpHeaders.AUTHORIZATION, bearer(jan)));

        // then
        result.andExpect(status().isMethodNotAllowed())
            .andExpect(header().exists(HttpHeaders.ALLOW));
    }

    private Registered register(String firstName, String lastName) throws Exception {
        String email = nextEmail();
        String idCardNo = nextIdCardNo();
        String response = mockMvc.perform(post("/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(patientBody(email, idCardNo, firstName, lastName, "1990-05-15")))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        long id = idOf(response);
        registeredIds.add(id);
        return new Registered(id, email, idCardNo);
    }

    private SharedAccount doctorWhoIsAlsoPatient() {
        String email = nextEmail();
        return transactionTemplate.execute(status -> {
            User user = userRepository.save(User.builder()
                .email(email)
                .rawPassword(PASSWORD)
                .passwordEncoder(passwordEncoder)
                .role(Role.DOCTOR)
                .build());
            Doctor doctor = doctorRepository.save(
                new Doctor(null, "Katarzyna", "Wrobel", Specialization.CARDIOLOGIST, user, new HashSet<>()));
            Patient patient = patientRepository.save(Patient.builder()
                .user(user)
                .idCardNo(nextIdCardNo())
                .firstName("Katarzyna")
                .lastName("Wrobel")
                .phoneNumber("123456789")
                .birthday(LocalDate.of(1982, 7, 14))
                .build());
            return new SharedAccount(user.getId(), doctor.getId(), patient.getId(), email);
        });
    }

    private String bearer(Registered patient) throws Exception {
        return "Bearer " + token(patient.email(), PASSWORD);
    }

    private String token(String email, String password) throws Exception {
        String response = mockMvc.perform(post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, password)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.token");
    }

    private int loginStatus(String email, String password) throws Exception {
        return mockMvc.perform(post("/auth/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, password)))
            .andReturn().getResponse().getStatus();
    }

    private static String loginBody(String email, String password) {
        return """
            {"email": "%s", "password": "%s"}
            """.formatted(email, password);
    }

    private static String patientBody(String email, String idCardNo, String firstName, String lastName, String birthday) {
        return """
            {
                "email": "%s",
                "password": "%s",
                "idCardNo": "%s",
                "firstName": "%s",
                "lastName": "%s",
                "phoneNumber": "123456789",
                "birthday": "%s"
            }
            """.formatted(email, PASSWORD, idCardNo, firstName, lastName, birthday);
    }

    private static String updateBody(String email, String firstName, String lastName, String birthday) {
        return """
            {
                "email": "%s",
                "firstName": "%s",
                "lastName": "%s",
                "phoneNumber": "987654321",
                "birthday": "%s"
            }
            """.formatted(email, firstName, lastName, birthday);
    }

    private static long idOf(String response) {
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private static String nextEmail() {
        return "api.patient" + SEQUENCE.incrementAndGet() + "@example.com";
    }

    private static String nextIdCardNo() {
        return "API%06d".formatted(SEQUENCE.incrementAndGet());
    }

    private record Registered(long id, String email, String idCardNo) {
    }

    private record SharedAccount(long userId, long doctorId, long patientId, String email) {
    }
}
