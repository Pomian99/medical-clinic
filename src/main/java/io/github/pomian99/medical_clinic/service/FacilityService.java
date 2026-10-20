package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.FacilityCreateCommand;
import io.github.pomian99.medical_clinic.exception.FacilityAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.FacilityNotFoundException;
import io.github.pomian99.medical_clinic.mapper.FacilityMapper;
import io.github.pomian99.medical_clinic.model.Facility;
import io.github.pomian99.medical_clinic.repository.FacilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacilityService {
    private final FacilityRepository facilityRepository;
    private final FacilityMapper mapper;

    @Transactional(readOnly = true)
    public List<Facility> findAll() {
        return facilityRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Facility findById(long id) {
        return facilityRepository.findById(id)
                .orElseThrow(() -> new FacilityNotFoundException(id));
    }

    @Transactional
    public Facility create(FacilityCreateCommand command) {
        // Business Validation: facilities.name is unique, so a taken name is refused.
        if (facilityRepository.existsByName(command.name())) {
            throw new FacilityAlreadyExistsException(command.name());
        }

        return facilityRepository.save(mapper.toEntity(command));
    }
}
