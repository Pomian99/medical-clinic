package io.github.pomian99.medical_clinic.repository;

import io.github.pomian99.medical_clinic.model.Doctor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Override
    @EntityGraph(attributePaths = {"user", "facilities"})
    List<Doctor> findAll();

    boolean existsByUserId(Long userId);
}
