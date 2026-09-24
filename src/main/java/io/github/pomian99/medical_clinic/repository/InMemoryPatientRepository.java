package io.github.pomian99.medical_clinic.repository;

import io.github.pomian99.medical_clinic.model.Patient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPatientRepository {
    private final List<Patient> patients = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Patient> findAll() {
        return List.copyOf(patients);
    }

    public Optional<Patient> findById(long id) {
        return patients.stream()
                .filter(patient -> patient.getId() == id)
                .findFirst();
    }

    public Optional<Patient> findByEmail(String email) {
        return patients.stream()
                .filter(patient -> patient.getEmail().equals(email))
                .findFirst();
    }

    public void save(Patient patient) {
        if (patient.getId() == null) {
            patient.setId(idCounter.getAndIncrement());
        }

        patients.add(patient);
    }

    public boolean deleteById(long id) {
        return patients.removeIf(patient -> patient.getId() == id);
    }
}