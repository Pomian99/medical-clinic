package io.github.pomian99.medical_clinic.mapper;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import io.github.pomian99.medical_clinic.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PatientMapper {

    PatientDto toDto(Patient patient);
    List<PatientDto> toDto(List<Patient> patients);

    @Mapping(target = "id", ignore = true)
    Patient toEntity(PatientCreateCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "idCardNo", ignore = true)
    void update(@MappingTarget Patient patient, PatientUpdateCommand command);
}
