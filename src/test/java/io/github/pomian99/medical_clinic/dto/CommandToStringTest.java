package io.github.pomian99.medical_clinic.dto;

import io.github.pomian99.medical_clinic.model.Role;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandToStringTest {

    @Test
    void patientCreateCommand_toString_hidesPasswordAndIdCardNo() {
        // given
        PatientCreateCommand command = new PatientCreateCommand(
            "jankow@poczta.pl",
            "Secret123!",
            "ABC123456",
            "Jan",
            "Kowalski",
            "123456789",
            LocalDate.of(1990, 5, 15)
        );

        // when
        String text = command.toString();

        // then
        assertFalse(text.contains("Secret123!"));
        assertFalse(text.contains("ABC123456"));
        assertTrue(text.contains("password=***"));
        assertTrue(text.contains("idCardNo=***"));
    }

    @Test
    void editPasswordCommand_toString_hidesPassword() {
        // given
        EditPasswordCommand command = new EditPasswordCommand("NewSecret123!");

        // when
        String text = command.toString();

        // then
        assertFalse(text.contains("NewSecret123!"));
        assertTrue(text.contains("password=***"));
    }

    @Test
    void userCreateCommand_toString_hidesPassword() {
        // given
        UserCreateCommand command = new UserCreateCommand("jankow@poczta.pl", "Secret123!", Role.DOCTOR);

        // when
        String text = command.toString();

        // then
        assertFalse(text.contains("Secret123!"));
        assertTrue(text.contains("password=***"));
    }

    @Test
    void loginCommand_toString_hidesPassword() {
        // given
        LoginCommand command = new LoginCommand("jankow@poczta.pl", "Secret123!");

        // when
        String text = command.toString();

        // then
        assertFalse(text.contains("Secret123!"));
        assertTrue(text.contains("password=***"));
    }
}
