package io.github.pomian99.medical_clinic.mapper;

import io.github.pomian99.medical_clinic.dto.FacilityCreateCommand;
import io.github.pomian99.medical_clinic.dto.FacilityDto;
import io.github.pomian99.medical_clinic.model.Facility;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FacilityMapper {
    FacilityDto toDto(Facility facility);

    List<FacilityDto> toDto(List<Facility> facilities);

    // A new facility has no doctors: they are assigned with POST /doctors/{id}/facilities/{facilityId}.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    Facility toEntity(FacilityCreateCommand command);
}
