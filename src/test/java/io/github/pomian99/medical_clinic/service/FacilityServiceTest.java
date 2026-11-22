package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.FacilityCreateCommand;
import io.github.pomian99.medical_clinic.dto.FacilityDto;
import io.github.pomian99.medical_clinic.exception.FacilityAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.FacilityNotFoundException;
import io.github.pomian99.medical_clinic.mapper.FacilityMapper;
import io.github.pomian99.medical_clinic.model.Facility;
import io.github.pomian99.medical_clinic.repository.FacilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

    @Mock
    FacilityRepository facilityRepository;

    FacilityService facilityService;

    @BeforeEach
    void setUp() {
        FacilityMapper mapper = Mappers.getMapper(FacilityMapper.class);
        facilityService = new FacilityService(facilityRepository, mapper);
    }

    @Test
    void findAll_twoFacilities_returnsBoth() {
        // given
        Facility krowodrza = new Facility(1L, "Przychodnia Krowodrza", "Kraków", "30-001", "Królewska", "12");
        Facility podgorze = new Facility(2L, "Przychodnia Podgórze", "Kraków", null, null, null);
        when(facilityRepository.findAll()).thenReturn(List.of(krowodrza, podgorze));

        // when
        List<FacilityDto> result = facilityService.findAll();

        // then
        assertEquals(
            List.of("Przychodnia Krowodrza", "Przychodnia Podgórze"),
            result.stream().map(FacilityDto::name).toList()
        );
    }

    @Test
    void findById_facilityExists_returnsAllFields() {
        // given
        Facility facility = new Facility(1L, "Przychodnia Krowodrza", "Kraków", "30-001", "Królewska", "12");
        when(facilityRepository.findById(1L)).thenReturn(Optional.of(facility));

        // when
        FacilityDto result = facilityService.findById(1L);

        // then
        assertEquals(
            new FacilityDto(1L, "Przychodnia Krowodrza", "Kraków", "30-001", "Królewska", "12"),
            result
        );
    }

    @Test
    void findById_facilityNotFound_throwsFacilityNotFound() {
        // given
        when(facilityRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        FacilityNotFoundException exception = assertThrows(
            FacilityNotFoundException.class,
            () -> facilityService.findById(1L)
        );

        // then
        assertEquals("Facility with id 1 not found", exception.getMessage());
    }

    @Test
    void create_newName_savesFacilityFromCommand() {
        // given
        FacilityCreateCommand command = new FacilityCreateCommand(
            "Przychodnia Krowodrza",
            "Kraków",
            "30-001",
            "Królewska",
            "12"
        );
        when(facilityRepository.existsByName("Przychodnia Krowodrza")).thenReturn(false);
        when(facilityRepository.save(any())).thenAnswer(invocation -> {
            Facility facility = invocation.getArgument(0);
            facility.setId(7L);
            return facility;
        });
        ArgumentCaptor<Facility> captor = ArgumentCaptor.forClass(Facility.class);

        // when
        FacilityDto result = facilityService.create(command);

        // then
        verify(facilityRepository).save(captor.capture());
        Facility saved = captor.getValue();
        assertEquals("Przychodnia Krowodrza", saved.getName());
        assertEquals("Kraków", saved.getCity());
        assertEquals("30-001", saved.getZipCode());
        assertEquals("Królewska", saved.getStreet());
        assertEquals("12", saved.getNumber());
        assertEquals(7L, result.id());
    }

    @Test
    void create_onlyRequiredFields_leavesAddressEmpty() {
        // given
        FacilityCreateCommand command = new FacilityCreateCommand("Przychodnia Podgórze", "Kraków", null, null, null);
        when(facilityRepository.existsByName("Przychodnia Podgórze")).thenReturn(false);
        when(facilityRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FacilityDto result = facilityService.create(command);

        // then
        assertEquals("Kraków", result.city());
        assertNull(result.zipCode());
        assertNull(result.street());
        assertNull(result.number());
    }

    @Test
    void create_nameTaken_throwsFacilityAlreadyExists() {
        // given
        FacilityCreateCommand command = new FacilityCreateCommand("Przychodnia Krowodrza", "Kraków", null, null, null);
        when(facilityRepository.existsByName("Przychodnia Krowodrza")).thenReturn(true);

        // when
        FacilityAlreadyExistsException exception = assertThrows(
            FacilityAlreadyExistsException.class,
            () -> facilityService.create(command)
        );

        // then
        assertEquals("Facility with name Przychodnia Krowodrza already exists", exception.getMessage());
        verify(facilityRepository, never()).save(any());
    }
}
