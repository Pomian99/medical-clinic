package io.github.pomian99.medical_clinic.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatientTest {

    @Test
    void hashCode_idAssignedAfterAddingToSet_setStillContainsPatient() {
        // given
        Patient patient = new Patient();
        Set<Patient> patients = new HashSet<>();
        patients.add(patient);

        // when
        patient.setId(42L);

        // then
        assertTrue(patients.contains(patient));
    }

    @Test
    void equals_sameId_patientsAreEqual() {
        // given
        Patient first = Patient.builder()
            .id(42L)
            .build();
        Patient second = Patient.builder()
            .id(42L)
            .build();

        // when
        boolean equal = first.equals(second);

        // then
        assertTrue(equal);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
