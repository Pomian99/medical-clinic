package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import io.github.pomian99.medical_clinic.exception.PatientNotFoundException;
import io.github.pomian99.medical_clinic.exception.UserAlreadyExistsException;
import io.github.pomian99.medical_clinic.mapper.PatientMapper;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.model.Role;
import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.repository.PatientRepository;
import io.github.pomian99.medical_clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper mapper;

    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    public Patient findById(long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    public Patient findByEmail(String email) {
        return patientRepository.findByUserEmail(email)
                .orElseThrow(() -> new PatientNotFoundException(email));
    }

    public List<Patient> findByFragment(String fragment) {
        return patientRepository.searchByFragment(fragment);
    }

    // Two inserts (users, then patients) must succeed or fail together: without a
    // transaction a failed patient insert would leave an account nobody can register to.
    public Patient create(PatientCreateCommand command) {
        // Business Validation: this rule requires checking other users,
        // so it cannot be handled by field-level validation annotations.
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }

        User user = User.builder()
            .email(command.email())
            .password(command.password())
            .role(Role.PATIENT)
            .build();

        Patient patient = mapper.toEntity(command);
        patient.setUser(user);

        return patientRepository.save(patient);
    }

    public Patient update(long id, PatientUpdateCommand command) {
        Patient patient = findById(id);

        mapper.update(patient, command);
        return patientRepository.save(patient);
    }

    public Patient updatePassword(long id, String password) {
        return patientRepository.findById(id)
                .map(patient -> {
                    patient.updatePassword(password);
                    return patientRepository.save(patient);
                })
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    public void delete(long id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        patientRepository.deleteById(id);
    }
}
