package io.github.pomian99.medical_clinic.repository;

import io.github.pomian99.medical_clinic.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    boolean existsByUserId(Long userId);
}
