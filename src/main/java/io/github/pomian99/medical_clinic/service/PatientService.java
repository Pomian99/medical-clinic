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

    public Patient create(Patient patient) {
        if (repository.findByEmail(patient.getEmail()).isPresent()) {
            throw new PatientAlreadyExistsException(String.format("Patient with email: %s already exists.", patient.getEmail()));
        }

        repository.save(patient);
        return patient;
    }
}
