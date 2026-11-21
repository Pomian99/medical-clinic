package io.github.pomian99.medical_clinic.repository;

import io.github.pomian99.medical_clinic.model.Patient;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    @Override
    @EntityGraph(attributePaths = {"user"})
    List<Patient> findAll();

    Optional<Patient> findByUserEmail(String email);

    @EntityGraph(attributePaths = {"user"})
    @Query(value = """
            select p from Patient p
            where lower(p.lastName) like lower(concat('%', :fragment, '%')) escape '!'
               or lower(p.firstName) like lower(concat('%', :fragment, '%')) escape '!'
            order by p.lastName, p.firstName, p.id
            """)
    List<Patient> searchByFragment(@Param("fragment") String fragment);

    boolean existsByUserId(long userId);

    boolean existsByIdCardNo(String idCardNo);
}
