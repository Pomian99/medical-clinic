package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import io.github.pomian99.medical_clinic.exception.PatientAlreadyExistsException;
import io.github.pomian99.medical_clinic.mapper.PatientMapper;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.repository.InMemoryPatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final InMemoryPatientRepository repository;
    private final PatientMapper mapper;

    public List<Patient> findAll() {
        return repository.findAll();
    }

    public Optional<Patient> findById(long id) {
        return repository.findById(id);
    }

    public Optional<Patient> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Patient create(PatientCreateCommand command) {
        // Business Validation: this rule requires checking other club members,
        // so it cannot be handled by field-level validation annotations.
        if (repository.findByEmail(command.email()).isPresent()) {
            throw new PatientAlreadyExistsException(command.email());
        }
        Patient patient = mapper.toEntity(command);
        repository.save(patient);
        return patient;
    }

    public Optional<Patient> update(long id, PatientUpdateCommand command) {
        return repository.findById(id)
                .map(existing -> {
                    mapper.update(existing, command);
                    return existing;
                });
    }

    public Optional<Patient> updatePassword(long id, String password) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setPassword(password);
                    return existing;
                });
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }
}
