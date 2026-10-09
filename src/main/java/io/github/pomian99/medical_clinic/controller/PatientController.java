package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.dto.EditPasswordCommand;
import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import io.github.pomian99.medical_clinic.mapper.PatientMapper;
import io.github.pomian99.medical_clinic.service.PatientService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Patients", description = "Medical clinic patients: create, read, update, delete")
@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;
    private final PatientMapper mapper;

    @Operation(summary = "List all patients")
    @GetMapping
    public List<PatientDto> getPatients() {
        return mapper.toDto(patientService.findAll());
    }

    @Operation(summary = "Find patient with given email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "No patient with this email exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping(params = "email")
    public PatientDto getPatientByEmail(@RequestParam String email) {
        return mapper.toDto(patientService.findByEmail(email));
    }

    @Operation(summary = "Find patients by a fragment of first or last name (case-insensitive)")
    @GetMapping(params = "fragment")
    public List<PatientDto> getPatientsByFragment(@RequestParam String fragment) {
        return mapper.toDto(patientService.findByFragment(fragment));
    }

    @Operation(summary = "Find patient with given id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient found"),
            @ApiResponse(
                    responseCode = "400",
                    description = "The id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No patient with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping("/{id}")
    public PatientDto getPatientById(@PathVariable long id) {
        return mapper.toDto(patientService.findById(id));
    }

    @Operation(summary = "Create new patient")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Patient created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request body failed validation or is not valid JSON",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "409",
                    description = "This email is already taken",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto createPatient(@Valid @RequestBody PatientCreateCommand command) {
        return mapper.toDto(patientService.create(command));
    }

    @Operation(summary = "Update data of patient with given id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient data updated"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request body failed validation or is not valid JSON, "
                            + "or the id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No patient with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "409",
                    description = "This email already belongs to another patient",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PutMapping("/{id}")
    public PatientDto updatePatient(
            @PathVariable long id,
            @Valid @RequestBody PatientUpdateCommand command
    ) {
        return mapper.toDto(patientService.update(id, command));
    }

    @Operation(summary = "Update password of patient with given id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patient password updated"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request body failed validation or is not valid JSON, "
                            + "or the id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No patient with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PatchMapping("/{id}/password")
    public PatientDto updatePatientPassword(
            @PathVariable long id,
            @Valid @RequestBody EditPasswordCommand command
    ) {
        return mapper.toDto(patientService.updatePassword(id, command.password()));
    }

    @Operation(summary = "Delete patient with given id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Patient deleted"),
            @ApiResponse(
                    responseCode = "400",
                    description = "The id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No patient with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@PathVariable long id) {
        patientService.delete(id);
    }
}
