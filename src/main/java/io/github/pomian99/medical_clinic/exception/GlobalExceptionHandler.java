package io.github.pomian99.medical_clinic.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
}
