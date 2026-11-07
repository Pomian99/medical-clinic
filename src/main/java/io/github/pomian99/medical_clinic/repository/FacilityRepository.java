package io.github.pomian99.medical_clinic.repository;

import io.github.pomian99.medical_clinic.model.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    boolean existsByName(String name);
}
