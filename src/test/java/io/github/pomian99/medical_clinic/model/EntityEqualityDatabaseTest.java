package io.github.pomian99.medical_clinic.model;

import io.github.pomian99.medical_clinic.repository.FacilityRepository;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EntityEqualityDatabaseTest {

    @Autowired
    FacilityRepository facilityRepository;

    Long savedId;

    @AfterEach
    void deleteSavedFacility() {
        if (savedId != null) {
            facilityRepository.deleteById(savedId);
        }
    }

    @Test
    void save_entityAddedToSetBeforeSave_setStillContainsItAfterIdIsAssigned() {
        // given
        Facility facility = facility();
        Set<Facility> facilities = new HashSet<>();
        facilities.add(facility);

        // when
        facilityRepository.save(facility);
        savedId = facility.getId();

        // then
        assertNotNull(savedId);
        assertTrue(facilities.contains(facility));
    }

    @Test
    void findById_sameRowReadTwice_givesTwoEqualObjects() {
        // given
        savedId = facilityRepository.save(facility()).getId();

        // when
        Facility first = facilityRepository.findById(savedId).orElseThrow();
        Facility second = facilityRepository.findById(savedId).orElseThrow();

        // then
        assertNotSame(first, second);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals(1, new HashSet<>(List.of(first, second)).size());
    }

    @Test
    void getReferenceById_uninitializedProxyOfSameRow_equalsLoadedEntity() {
        // given
        savedId = facilityRepository.save(facility()).getId();
        Facility loaded = facilityRepository.findById(savedId).orElseThrow();

        // when
        Facility reference = facilityRepository.getReferenceById(savedId);

        // then
        assertInstanceOf(HibernateProxy.class, reference);
        assertNotSame(Facility.class, reference.getClass());
        assertTrue(loaded.equals(reference));
    }

    private static Facility facility() {
        return new Facility(null, "Equality Guard Facility", "Kraków", null, null, null);
    }
}
