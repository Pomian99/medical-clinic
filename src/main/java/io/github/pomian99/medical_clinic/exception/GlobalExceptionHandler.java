package io.github.pomian99.medical_clinic.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MedicalClinicException.class)
    public ProblemDetail handleMedicalClinic(MedicalClinicException exception) {
        log.warn("Domain error: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(
                exception.getStatus(), exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        List<Map<String, String>> errors = exception.getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> {
                    Map<String, String> entry = new LinkedHashMap<>();
                    entry.put("field", error.getField());
                    entry.put("message", String.valueOf(error.getDefaultMessage()));
                    return entry;
                })
                .toList();
        log.warn("Request rejected: {} validation rules failed", errors.size());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "request body failed validation"
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.warn("Wrong type in path variable: {}", exception.getName());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "path variable " + exception.getName() + " has a wrong type"
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException exception) {
        log.warn("Unreadable request body");
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "request body is not valid JSON"
        );
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoRoute(NoResourceFoundException exception) {
        log.warn("Route not found: {}", exception.getResourcePath());
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "no such endpoint"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknown(Exception exception) {
        log.error("Unhandled exception", exception);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unknown error"
        );
    }
}
