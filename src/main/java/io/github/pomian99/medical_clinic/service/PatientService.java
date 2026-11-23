package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import io.github.pomian99.medical_clinic.exception.PatientAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.PatientHasVisitsException;
import io.github.pomian99.medical_clinic.exception.PatientNotFoundException;
import io.github.pomian99.medical_clinic.exception.UserAlreadyExistsException;
import io.github.pomian99.medical_clinic.mapper.PatientMapper;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.model.Role;
import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.repository.DoctorRepository;
import io.github.pomian99.medical_clinic.repository.PatientRepository;
import io.github.pomian99.medical_clinic.repository.UserRepository;
import io.github.pomian99.medical_clinic.repository.VisitRepository;
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
    private final VisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
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
        Patient patient = patientRepository.findByUserEmail(User.normalizeEmail(email))
                .orElseThrow(PatientNotFoundException::new);
        return mapper.toDto(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientDto> findByFragment(String fragment) {
        return mapper.toDto(patientRepository.searchByFragment(escapeLikeWildcards(fragment)));
    }

    // Two inserts (users, then patients) must succeed or fail together: without a
    // transaction a failed patient insert would leave an account nobody can register to.
    @Transactional
    public PatientDto create(PatientCreateCommand command) {
        // Business Validation: this rule requires checking other users,
        // so it cannot be handled by field-level validation annotations.
        if (userRepository.existsByEmail(User.normalizeEmail(command.email()))) {
            throw new UserAlreadyExistsException();
        }
        if (patientRepository.existsByIdCardNo(command.idCardNo())) {
            throw new PatientAlreadyExistsException();
        }

        User user = User.builder()
            .email(command.email())
            .rawPassword(command.password())
            .passwordEncoder(passwordEncoder)
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
        User user = patient.getUser();
        String email = User.normalizeEmail(command.email());
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException();
        }

        mapper.update(patient, command);
        user.setEmail(email);
        log.info("Updated patient {}", id);
        return mapper.toDto(patient);
    }

    @Transactional
    public PatientDto changePassword(long id, String password) {
        Patient patient = getPatient(id);
        patient.changePassword(password, passwordEncoder);
        log.info("Changed password of patient {}", id);
        return mapper.toDto(patient);
    }

    @Transactional
    public void deleteById(long id) {
        Patient patient = getPatient(id);
        if (visitRepository.existsByPatientId(id)) {
            throw new PatientHasVisitsException(id);
        }

        User user = patient.getUser();
        patientRepository.delete(patient);
        if (doctorRepository.existsByUserId(user.getId())) {
            log.info("Deleted patient {}, account {} stays with its doctor", id, user.getId());
        } else {
            userRepository.delete(user);
            log.info("Deleted patient {} and account {}", id, user.getId());
        }
    }

    private Patient getPatient(long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    private static String escapeLikeWildcards(String fragment) {
        return fragment
            .replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_");
    }
}
