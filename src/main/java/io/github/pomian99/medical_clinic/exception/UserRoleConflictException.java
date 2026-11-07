package io.github.pomian99.medical_clinic.exception;

import io.github.pomian99.medical_clinic.model.Role;
import org.springframework.http.HttpStatus;

public class UserRoleConflictException extends MedicalClinicException {
    public UserRoleConflictException(long userId, Role role) {
        super(
                String.format("User with id %d already has role %s", userId, role),
                HttpStatus.CONFLICT
        );
    }
}
