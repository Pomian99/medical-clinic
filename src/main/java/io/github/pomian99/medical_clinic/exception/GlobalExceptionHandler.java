package io.github.pomian99.medical_clinic.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException.ConstraintKind;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Map<String, String> CONFLICT_MESSAGES = Map.of(
        "uk_visits_doctor_id_starts_at", "The doctor already has a visit starting at this time",
        "uk_users_email", "User with this email already exists",
        "uk_patients_id_card_no", "Patient with this ID card number already exists",
        "uk_facilities_name", "Facility with this name already exists"
    );

    @ExceptionHandler(MedicalClinicException.class)
    public ProblemDetail handleMedicalClinic(MedicalClinicException exception) {
        log.warn("Domain error: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(
                exception.getStatus(), exception.getMessage()
        );
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleVersionConflict(ObjectOptimisticLockingFailureException exception) {
        log.warn("Version conflict on {} with id {}", exception.getPersistentClassName(), exception.getIdentifier());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "The resource was changed by another request at the same time. Fetch it again and retry."
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException exception) {
        if (exception.getCause() instanceof ConstraintViolationException violation
            && (violation.getKind() == ConstraintKind.UNIQUE || violation.getKind() == ConstraintKind.FOREIGN_KEY)) {
            log.warn("Request conflicts with stored data: {} constraint {}", violation.getKind(), violation.getConstraintName());
            return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                conflictMessage(violation.getConstraintName())
            );
        }
        return handleUnknown(exception);
    }

    private static String conflictMessage(String constraintName) {
        if (constraintName != null) {
            String name = constraintName.toLowerCase(Locale.ROOT);
            for (Map.Entry<String, String> entry : CONFLICT_MESSAGES.entrySet()) {
                if (name.contains(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return "The request conflicts with data already stored";
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        List<FieldViolation> errors = exception.getFieldErrors().stream()
            .sorted(Comparator.comparing(FieldError::getField))
            .map(error -> new FieldViolation(error.getField(), error.getDefaultMessage()))
            .toList();
        log.warn("Request rejected: {} validation rules failed", errors.size());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            status,
            "Request body failed validation"
        );
        problem.setProperty("errors", errors);
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
        TypeMismatchException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        log.warn("Wrong type in path variable: {}", exception.getPropertyName());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            status,
            "Path variable " + exception.getPropertyName() + " has a wrong type"
        );
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
        MissingServletRequestParameterException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        log.warn("Missing request parameter: {}", exception.getParameterName());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            status,
            "Request parameter " + exception.getParameterName() + " is missing"
        );
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        if (exception.getCause() instanceof MismatchedInputException mismatch && !mismatch.getPath().isEmpty()) {
            String field = fieldPath(mismatch.getPath());
            log.warn("Request rejected: field {} has a wrong type or format", field);
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                status,
                "Request body has a field with a wrong type or format"
            );
            problem.setProperty("errors", List.of(new FieldViolation(field, "Wrong type or format")));
            return handleExceptionInternal(exception, problem, headers, status, request);
        }
        log.warn("Unreadable request body");
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            status,
            "Request body is not valid JSON"
        );
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    private static String fieldPath(List<JacksonException.Reference> path) {
        StringBuilder field = new StringBuilder();
        for (JacksonException.Reference reference : path) {
            if (reference.getPropertyName() != null) {
                if (!field.isEmpty()) {
                    field.append('.');
                }
                field.append(reference.getPropertyName());
            } else {
                field.append('[').append(reference.getIndex()).append(']');
            }
        }
        return field.toString();
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        log.warn("Method not supported: {}", exception.getMethod());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            status,
            "Method " + exception.getMethod() + " is not supported here"
        );
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        log.warn("Unsupported content type: {}", exception.getContentType());
        return super.handleHttpMediaTypeNotSupported(exception, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
        HttpMediaTypeNotAcceptableException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        log.warn("No acceptable media type, supported: {}", exception.getSupportedMediaTypes());
        return super.handleHttpMediaTypeNotAcceptable(exception, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
        NoResourceFoundException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        log.warn("Route not found: {}", exception.getResourcePath());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            status,
            "No such endpoint"
        );
        return handleExceptionInternal(exception, problem, headers, status, request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException exception) {
        log.warn("Login failed: {}", exception.getClass().getSimpleName());
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "wrong e-mail or password"
        );
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception exception,
        Object body,
        HttpHeaders headers,
        HttpStatusCode statusCode,
        WebRequest request
    ) {
        if (statusCode.is5xxServerError()) {
            log.error("Unhandled exception", exception);
            body = ProblemDetail.forStatusAndDetail(statusCode, "Unknown error");
        }
        return super.handleExceptionInternal(exception, body, headers, statusCode, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknown(Exception exception) {
        log.error("Unhandled exception", exception);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unknown error"
        );
    }

    private record FieldViolation(String field, String message) {
    }
}
