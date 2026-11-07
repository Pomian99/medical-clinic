package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.dto.FacilityCreateCommand;
import io.github.pomian99.medical_clinic.dto.FacilityDto;
import io.github.pomian99.medical_clinic.mapper.FacilityMapper;
import io.github.pomian99.medical_clinic.service.FacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Facilities", description = "Medical clinic facilities: create, read")
@RestController
@RequestMapping("/facilities")
@RequiredArgsConstructor
public class FacilityController {
    private final FacilityService facilityService;
    private final FacilityMapper mapper;

    @Operation(summary = "List all facilities")
    @GetMapping
    public List<FacilityDto> getFacilities() {
        return mapper.toDto(facilityService.findAll());
    }

    @Operation(summary = "Find facility with given id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Facility found"),
            @ApiResponse(
                    responseCode = "400",
                    description = "The id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No facility with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping("/{id}")
    public FacilityDto getFacilityById(@PathVariable long id) {
        return mapper.toDto(facilityService.findById(id));
    }

    @Operation(
            summary = "Create new facility",
            description = "Only the name and the city are required. Doctors are assigned "
                    + "afterwards with POST /doctors/{id}/facilities/{facilityId}."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Facility created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request body failed validation or is not valid JSON",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "409",
                    description = "This name already belongs to a facility",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacilityDto createFacility(@Valid @RequestBody FacilityCreateCommand command) {
        return mapper.toDto(facilityService.create(command));
    }
}
