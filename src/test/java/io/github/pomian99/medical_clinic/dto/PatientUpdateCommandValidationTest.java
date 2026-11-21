package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.pomian99.medical_clinic.dto.ValidationRules.LAST_NAME_PATTERN_MESSAGE;
import static io.github.pomian99.medical_clinic.dto.ValidationRules.PHONE_NUMBER_PATTERN_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatientUpdateCommandValidationTest {

    static final LocalDate BIRTHDAY = LocalDate.of(1990, 5, 15);

    static ValidatorFactory factory;
    static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        factory.close();
    }

    @Test
    void validate_allFieldsValid_noViolations() {
        // given
        PatientUpdateCommand command = new PatientUpdateCommand("Janusz", "Nowak", "987654321", BIRTHDAY);

        // when
        Set<ConstraintViolation<PatientUpdateCommand>> violations = validator.validate(command);

        // then
        assertTrue(violations.isEmpty());
    }

    @Test
    void validate_blankFirstName_reportsFirstName() {
        // given
        PatientUpdateCommand command = new PatientUpdateCommand("", "Nowak", "987654321", BIRTHDAY);

        // when
        Set<ConstraintViolation<PatientUpdateCommand>> violations = validator.validate(command);

        // then
        assertEquals(Set.of("firstName"), fields(violations));
        assertTrue(messages(violations, "firstName").contains("First name cannot be blank"));
    }

    @Test
    void validate_lastNameWithDigit_reportsLastNamePattern() {
        // given
        PatientUpdateCommand command = new PatientUpdateCommand("Janusz", "Nowak1", "987654321", BIRTHDAY);

        // when
        Set<ConstraintViolation<PatientUpdateCommand>> violations = validator.validate(command);

        // then
        assertEquals(Set.of(LAST_NAME_PATTERN_MESSAGE), messages(violations, "lastName"));
    }

    @Test
    void validate_phoneNumberTooShort_reportsPhoneNumberPattern() {
        // given
        PatientUpdateCommand command = new PatientUpdateCommand("Janusz", "Nowak", "500", BIRTHDAY);

        // when
        Set<ConstraintViolation<PatientUpdateCommand>> violations = validator.validate(command);

        // then
        assertEquals(Set.of(PHONE_NUMBER_PATTERN_MESSAGE), messages(violations, "phoneNumber"));
    }

    @Test
    void validate_noBirthday_reportsBirthdayRequired() {
        // given
        PatientUpdateCommand command = new PatientUpdateCommand("Janusz", "Nowak", "987654321", null);

        // when
        Set<ConstraintViolation<PatientUpdateCommand>> violations = validator.validate(command);

        // then
        assertEquals(Set.of("Birthday is required"), messages(violations, "birthday"));
    }

    @Test
    void validate_birthdayTomorrow_reportsBirthdayInPast() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        PatientUpdateCommand command = new PatientUpdateCommand("Janusz", "Nowak", "987654321", tomorrow);

        // when
        Set<ConstraintViolation<PatientUpdateCommand>> violations = validator.validate(command);

        // then
        assertEquals(Set.of("Birthday must be a date in the past"), messages(violations, "birthday"));
    }

    private static Set<String> fields(Set<ConstraintViolation<PatientUpdateCommand>> violations) {
        return violations.stream()
            .map(violation -> violation.getPropertyPath().toString())
            .collect(Collectors.toSet());
    }

    private static Set<String> messages(Set<ConstraintViolation<PatientUpdateCommand>> violations, String field) {
        return violations.stream()
            .filter(violation -> violation.getPropertyPath().toString().equals(field))
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toSet());
    }
}
