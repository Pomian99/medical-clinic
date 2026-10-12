package io.github.pomian99.medical_clinic.repository;

import io.github.pomian99.medical_clinic.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    boolean existsByDoctorId(long doctorId);
}
