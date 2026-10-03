package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import io.github.pomian99.medical_clinic.exception.PatientAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.PatientNotFoundException;
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

    public Patient findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    public Patient findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new PatientNotFoundException(email));
    }

    public Patient create(PatientCreateCommand command) {
        // Business Validation: this rule requires checking other patients,
        // so it cannot be handled by field-level validation annotations.
        if (repository.findByEmail(command.email()).isPresent()) {
            throw new PatientAlreadyExistsException(command.email());
        }

        Patient patient = mapper.toEntity(command);
        repository.save(patient);
        return patient;
    }

    public Patient update(long id, PatientUpdateCommand command) {
        Patient patient = findById(id);

        // Business Validation: this rule requires checking other patients,
        // so it cannot be handled by field-level validation annotations.
        // The patient may keep their own email, but it cannot belong to someone else.
        Optional<Patient> emailOwner = repository.findByEmail(command.email());
        if (emailOwner.isPresent() && !emailOwner.get().getId().equals(id)) {
            throw new PatientAlreadyExistsException(command.email());
        }

        mapper.update(patient, command);
        return patient;
    }

    public Patient updatePassword(long id, String password) {
        return repository.findById(id)
                .map(patient -> {
                    patient.updatePassword(password);
                    return patient;
                })
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    public void delete(long id) {
        if (!repository.deleteById(id)) {
            throw new PatientNotFoundException(id);
        }
    }
}
