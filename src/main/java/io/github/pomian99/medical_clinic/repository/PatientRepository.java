package io.github.pomian99.medical_clinic.repository;

import io.github.pomian99.medical_clinic.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);

    boolean existsByEmail(String email);

    // Spring Data builds the query from the method name: where email = ? and id <> ?.
    // True when the email belongs to some other patient. The patient with the given id
    // is left out, so a patient who keeps their own email on update is not a conflict.
    boolean existsByEmailAndIdNot(String email, Long id);

    @Query(value = """
            select p from Patient p
            where lower(p.lastName) like lower(concat('%', :fragment, '%'))
               or lower(p.firstName) like lower(concat('%', :fragment, '%'))
            """)
    List<Patient> searchByFragment(@Param("fragment") String fragment);
}
