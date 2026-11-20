package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<PatientDto> findAll() {
        return mapper.toDto(patientRepository.findAll());
    }

    @Transactional(readOnly = true)
    public PatientDto findById(long id) {
        return mapper.toDto(getPatient(id));
    }

    @Transactional(readOnly = true)
    public PatientDto findByEmail(String email) {
        Patient patient = patientRepository.findByUserEmail(email)
                .orElseThrow(PatientNotFoundException::new);
        return mapper.toDto(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientDto> findByFragment(String fragment) {
        return mapper.toDto(patientRepository.searchByFragment(fragment));
    }

    // Two inserts (users, then patients) must succeed or fail together: without a
    // transaction a failed patient insert would leave an account nobody can register to.
    @Transactional
    public PatientDto create(PatientCreateCommand command) {
        // Business Validation: this rule requires checking other users,
        // so it cannot be handled by field-level validation annotations.
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException();
        }

        User user = User.builder()
            .email(command.email())
            .password(passwordEncoder.encode(command.password()))
            .role(Role.PATIENT)
            .build();

        Patient patient = mapper.toEntity(command);
        patient.setUser(user);

        Patient saved = patientRepository.save(patient);
        log.info("Registered patient {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Transactional
    public PatientDto update(long id, PatientUpdateCommand command) {
        Patient patient = getPatient(id);

        mapper.update(patient, command);
        log.info("Updated patient {}", id);
        return mapper.toDto(patient);
    }

    @Transactional
    public PatientDto changePassword(long id, String password) {
        Patient patient = getPatient(id);
        patient.updatePassword(passwordEncoder.encode(password));
        log.info("Changed password of patient {}", id);
        return mapper.toDto(patient);
    }

    @Transactional
    public void deleteById(long id) {
        if (!patientRepository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        patientRepository.deleteById(id);
        log.info("Deleted patient {}", id);
    }

    private Patient getPatient(long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }
}
