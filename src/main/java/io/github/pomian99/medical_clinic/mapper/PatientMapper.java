package io.github.pomian99.medical_clinic.mapper;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import io.github.pomian99.medical_clinic.model.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PatientMapper {

    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "fullName", source = ".", qualifiedByName = "joinName")
    PatientDto toDto(Patient patient);

    List<PatientDto> toDto(List<Patient> patients);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Patient toEntity(PatientCreateCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idCardNo", ignore = true)
    @Mapping(target = "user", ignore = true)
    void update(@MappingTarget Patient patient, PatientUpdateCommand command);

    @Named("joinName")
    default String joinName(Patient patient) {
        return patient.getFirstName() + " " + patient.getLastName();
    }
}
