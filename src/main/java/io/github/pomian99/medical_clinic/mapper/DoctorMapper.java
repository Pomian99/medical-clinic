package io.github.pomian99.medical_clinic.mapper;

import io.github.pomian99.medical_clinic.dto.DoctorCreateCommand;
import io.github.pomian99.medical_clinic.dto.DoctorDto;
import io.github.pomian99.medical_clinic.model.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface DoctorMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    DoctorDto toDto(Doctor doctor);

    List<DoctorDto> toDto(List<Doctor> doctors);

    // The command carries only userId; the service loads the account and sets it.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "visits", ignore = true)
    Doctor toEntity(DoctorCreateCommand command);
}
