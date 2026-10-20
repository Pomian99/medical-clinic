package io.github.pomian99.medical_clinic.mapper;

import io.github.pomian99.medical_clinic.dto.UserCreateCommand;
import io.github.pomian99.medical_clinic.dto.UserDto;
import io.github.pomian99.medical_clinic.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserMapper {
    UserDto toDto(User user);
    List<UserDto> toDto(List<User> users);

    @Mapping(target = "id", ignore = true)
    User toEntity(UserCreateCommand command);
}
