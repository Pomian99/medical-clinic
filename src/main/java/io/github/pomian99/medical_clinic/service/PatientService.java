package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.exception.PatientAlreadyExistsException;
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

    public List<Patient> findAll() {
        return repository.findAll();
    }

    public Optional<Patient> findById(long id) {
        return repository.findById(id);
    }

    public Optional<Patient> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Patient create(Patient patient) {
        if (repository.findByEmail(patient.getEmail()).isPresent()) {
            throw new PatientAlreadyExistsException(String.format("Patient with email: %s already exists.", patient.getEmail()));
        }

        repository.save(patient);
        return patient;
    }

    public Optional<Patient> update(long id, Patient patient) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setEmail(patient.getEmail());
                    existing.setPassword(patient.getPassword());
                    existing.setIdCardNo(patient.getIdCardNo());
                    existing.setFirstName(patient.getFirstName());
                    existing.setLastName(patient.getLastName());
                    existing.setPhoneNumber(patient.getPhoneNumber());
                    existing.setBirthday(patient.getBirthday());
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
