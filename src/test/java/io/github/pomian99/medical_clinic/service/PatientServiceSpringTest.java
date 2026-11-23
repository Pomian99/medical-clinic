package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class PatientServiceSpringTest {

    @Autowired
    PatientService patientService;

    Long createdId;

    @AfterEach
    void deleteCreatedPatient() {
        if (createdId != null) {
            patientService.deleteById(createdId);
        }
    }

    @Test
    void create_newEmail_patientIsReadBackFromDatabase() {
        // given
        PatientCreateCommand command = new PatientCreateCommand(
            "Spring.Test@Example.com",
            "Secret123!",
            "SPR123456",
            "Jan",
            "Kowalski",
            "123456789",
            LocalDate.of(1990, 5, 15)
        );

        // when
        PatientDto created = patientService.create(command);
        createdId = created.id();
        PatientDto found = patientService.findById(created.id());

        // then
        assertEquals("spring.test@example.com", found.email());
        assertEquals("Jan Kowalski", found.fullName());
        assertEquals(LocalDate.of(1990, 5, 15), found.birthday());
    }
}
