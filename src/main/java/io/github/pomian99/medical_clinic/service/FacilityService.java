package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.FacilityCreateCommand;
import io.github.pomian99.medical_clinic.dto.FacilityDto;
import io.github.pomian99.medical_clinic.exception.FacilityAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.FacilityNotFoundException;
import io.github.pomian99.medical_clinic.mapper.FacilityMapper;
import io.github.pomian99.medical_clinic.model.Facility;
import io.github.pomian99.medical_clinic.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {
    private final FacilityRepository facilityRepository;
    private final FacilityMapper mapper;

    @Transactional(readOnly = true)
    public List<FacilityDto> findAll() {
        return mapper.toDto(facilityRepository.findAll());
    }

    @Transactional(readOnly = true)
    public FacilityDto findById(long id) {
        Facility facility = facilityRepository.findById(id)
                .orElseThrow(() -> new FacilityNotFoundException(id));
        return mapper.toDto(facility);
    }

    @Transactional
    public FacilityDto create(FacilityCreateCommand command) {
        // Business Validation: facilities.name is unique, so a taken name is refused.
        if (facilityRepository.existsByName(command.name())) {
            throw new FacilityAlreadyExistsException(command.name());
        }

        Facility saved = facilityRepository.save(mapper.toEntity(command));
        log.info("Created facility {}", saved.getId());
        return mapper.toDto(saved);
    }
}
