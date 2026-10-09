package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import io.github.pomian99.medical_clinic.exception.PatientAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.PatientNotFoundException;
import io.github.pomian99.medical_clinic.mapper.PatientMapper;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {
    private final PatientRepository repository;
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
        if (repository.existsByEmail(command.email())) {
            throw new PatientAlreadyExistsException(command.email());
        }

        return repository.save(mapper.toEntity(command));
    }

    public Patient update(long id, PatientUpdateCommand command) {
        // Business Validation: this rule requires checking other patients,
        // so it cannot be handled by field-level validation annotations.
        // The patient may keep their own email, but it cannot belong to someone else.
        if (repository.existsByEmailAndIdNot(command.email(), id)) {
            throw new PatientAlreadyExistsException(command.email());
        }
        Patient patient = findById(id);

        mapper.update(patient, command);
        return repository.save(patient);
    }

    public Patient updatePassword(long id, String password) {
        return repository.findById(id)
                .map(patient -> {
                    patient.updatePassword(password);
                    return repository.save(patient);
                })
                .orElseThrow(() -> new PatientNotFoundException(id));
    }

    public void delete(long id) {
        if (!repository.existsById(id)) {
            throw new PatientNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
