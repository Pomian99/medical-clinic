package io.github.pomian99.medical_clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "visits")
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startsAt;

    @Column(nullable = false)
    private LocalDateTime endsAt;

    // Many visits share one doctor, so there is no unique constraint on doctor_id.
    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // A visit without a patient is a free slot.
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;
}
