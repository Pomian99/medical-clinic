package io.github.pomian99.medical_clinic.repository;

import io.github.pomian99.medical_clinic.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    boolean existsByDoctorId(long doctorId);
    List<Visit> findAllByDoctorId(long doctorId);
    List<Visit> findAllByDoctorIdOrderByStartsAt(long doctorId);
    List<Visit> findAllByPatientId(long patientId);
    boolean existsByDoctorIdAndStartsAtBeforeAndEndsAtAfter(long doctorId, LocalDateTime end, LocalDateTime start);

    @Query("""
        select count(v) from Visit v
        where v.doctor.id = :doctorId and v.patient is null
          and v.startsAt >= :from and v.startsAt < :to
        """)
    long countFreeSlots(
        @Param("doctorId") long doctorId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}
