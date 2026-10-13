package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.dto.DoctorCreateCommand;
import io.github.pomian99.medical_clinic.dto.DoctorDto;
import io.github.pomian99.medical_clinic.mapper.DoctorMapper;
import io.github.pomian99.medical_clinic.service.DoctorService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Doctors", description = "Medical clinic doctors: create, read, delete, assign facilities")
@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;
    private final DoctorMapper mapper;

    @Operation(summary = "List all doctors")
    @GetMapping
    public List<DoctorDto> getDoctors() {
        return mapper.toDto(doctorService.findAll());
    }

    @Operation(summary = "Find doctor with given id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doctor found"),
            @ApiResponse(
                    responseCode = "400",
                    description = "The id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No doctor with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping("/{id}")
    public DoctorDto getDoctorById(@PathVariable long id) {
        return mapper.toDto(doctorService.findById(id));
    }

    @Operation(
            summary = "Create new doctor",
            description = "Makes an existing account (created with POST /users) a doctor. "
                    + "An account without a role gets the DOCTOR role."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Doctor created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request body failed validation or is not valid JSON",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No user with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "409",
                    description = "This account already has a doctor or a role other than DOCTOR",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorDto createDoctor(@Valid @RequestBody DoctorCreateCommand command) {
        return mapper.toDto(doctorService.create(command));
    }

    @Operation(
            summary = "Assign facility to doctor",
            description = "Repeating the request is safe: a doctor is assigned to a facility at most once."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Facility assigned, the doctor is returned"),
            @ApiResponse(
                    responseCode = "400",
                    description = "An id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No doctor or no facility with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PostMapping(path = "/{id}/facilities/{facilityId}")
    public DoctorDto addFacility(@PathVariable long id, @PathVariable long facilityId) {
        return mapper.toDto(doctorService.addFacility(id, facilityId));
    }

    @Operation(
            summary = "Unassign facility from doctor",
            description = "Removes only the assignment; the doctor and the facility stay. "
                    + "Unassigning a facility the doctor does not have changes nothing."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Facility unassigned, the doctor is returned"),
            @ApiResponse(
                    responseCode = "400",
                    description = "An id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No doctor or no facility with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @DeleteMapping(path = "/{id}/facilities/{facilityId}")
    public DoctorDto removeFacility(@PathVariable long id, @PathVariable long facilityId) {
        return mapper.toDto(doctorService.removeFacility(id, facilityId));
    }

    @Operation(
            summary = "Delete doctor with given id",
            description = "Only a doctor without visits can be deleted. The account stays."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Doctor deleted"),
            @ApiResponse(
                    responseCode = "400",
                    description = "The id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No doctor with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "409",
                    description = "The doctor has visits, so it cannot be deleted",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDoctor(@PathVariable long id) {
        doctorService.delete(id);
    }
}
