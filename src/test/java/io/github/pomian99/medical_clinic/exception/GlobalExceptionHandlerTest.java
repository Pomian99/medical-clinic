package io.github.pomian99.medical_clinic.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDataIntegrity_visitStartTaken_returnsConflictWithVisitMessage() {
        // given
        DataIntegrityViolationException exception = uniqueViolation("uk_visits_doctor_id_starts_at");

        // when
        ProblemDetail problem = handler.handleDataIntegrity(exception);

        // then
        assertEquals(409, problem.getStatus());
        assertEquals("The doctor already has a visit starting at this time", problem.getDetail());
    }

    @Test
    void handleDataIntegrity_constraintNameInUppercase_returnsSameMessage() {
        // given
        DataIntegrityViolationException exception = uniqueViolation("PUBLIC.UK_VISITS_DOCTOR_ID_STARTS_AT_INDEX_9");

        // when
        ProblemDetail problem = handler.handleDataIntegrity(exception);

        // then
        assertEquals("The doctor already has a visit starting at this time", problem.getDetail());
    }

    @Test
    void handleDataIntegrity_unknownConstraint_returnsGeneralConflict() {
        // given
        DataIntegrityViolationException exception = uniqueViolation("uk_something_else");

        // when
        ProblemDetail problem = handler.handleDataIntegrity(exception);

        // then
        assertEquals(409, problem.getStatus());
        assertEquals("The request conflicts with data already stored", problem.getDetail());
    }

    private static DataIntegrityViolationException uniqueViolation(String constraintName) {
        ConstraintViolationException violation = new ConstraintViolationException(
            "could not execute statement",
            new SQLException("duplicate key value violates unique constraint \"" + constraintName + "\""),
            "insert into visits (doctor_id, starts_at) values (?, ?)",
            ConstraintKind.UNIQUE,
            constraintName
        );
        return new DataIntegrityViolationException(violation.getMessage(), violation);
    }
}
