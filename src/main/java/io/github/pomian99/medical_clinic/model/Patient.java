package io.github.pomian99.medical_clinic.model;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Patient {
    private Long id;
    private String email;
    private String password;
    private String idCardNo;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthday;

    public static Patient create(PatientCreateCommand command) {
        Patient patient = new Patient();
        patient.setEmail(command.email());
        patient.setPassword(command.password());
        patient.setIdCardNo(command.idCardNo());
        patient.setFirstName(command.firstName());
        patient.setLastName(command.lastName());
        patient.setBirthday(command.birthday());
        return patient;
    }

    public void update(PatientUpdateCommand command) {
        email = command.email();
        firstName = command.firstName();
        lastName = command.lastName();
        phoneNumber = command.phoneNumber();
        birthday = command.birthday();
    }

}
