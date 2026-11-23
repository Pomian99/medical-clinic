package io.github.pomian99.medical_clinic.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DoctorTest {

    @Test
    void removeFacility_facilityAddedBeforeIdAssigned_facilitiesAreEmpty() {
        // given
        Doctor doctor = new Doctor();
        Facility facility = new Facility();
        doctor.addFacility(facility);
        facility.setId(7L);

        // when
        doctor.removeFacility(facility);

        // then
        assertTrue(doctor.getFacilities().isEmpty());
    }
}
