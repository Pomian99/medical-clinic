package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.dto.EditPasswordCommand;
import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import io.github.pomian99.medical_clinic.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
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

    @Operation(
            summary = "List patients, or find them by email or by a fragment of the name",
            description = "One path with three variants, chosen by the query parameter (send at most one):\n\n"
                    + "- no parameter: all patients;\n"
                    + "- `email`: the patient with this email, letter case ignored; one patient, not a list;\n"
                    + "- `fragment`: patients whose first or last name contains the fragment, letter case "
                    + "ignored, sorted by last name, first name and id; `%` and `_` are searched for literally.",
            parameters = {
                    @Parameter(
                            name = "email",
                            in = ParameterIn.QUERY,
                            required = false,
                            description = "Email of the patient; the response is one patient instead of a list",
                            schema = @Schema(type = "string")
                    ),
                    @Parameter(
                            name = "fragment",
                            in = ParameterIn.QUERY,
                            required = false,
                            description = "Fragment of the first or last name",
                            schema = @Schema(type = "string")
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "A list of patients, or one patient when email is given",
                    content = @Content(
                            schema = @Schema(oneOf = {PatientDto[].class, PatientDto.class})
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "Only with email: no patient with this email exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping
    public List<PatientDto> getPatients() {
        return patientService.findAll();
    }

    @Operation(hidden = true)
    @GetMapping(params = "email")
    public PatientDto getPatientByEmail(@RequestParam String email) {
        return patientService.findByEmail(email);
    }

    @Operation(hidden = true)
    @GetMapping(params = "fragment")
    public List<PatientDto> getPatientsByFragment(@RequestParam String fragment) {
        return patientService.findByFragment(fragment);
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
        return patientService.findById(id);
    }

    @Operation(
            summary = "Create new patient",
            description = "Registration is for visitors who are not logged in: a request with an access token gets 403."
    )
    @SecurityRequirements
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
                    description = "This email or ID card number is already taken",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto createPatient(@Valid @RequestBody PatientCreateCommand command) {
        return patientService.create(command);
    }

    @Operation(
            summary = "Update data of patient with given id",
            description = "Replaces all data except the password and the ID card number. "
                    + "The email is changed on the patient's account."
    )
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
                    description = "This email belongs to another account",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PutMapping("/{id}")
    public PatientDto updatePatient(
            @PathVariable long id,
            @Valid @RequestBody PatientUpdateCommand command
    ) {
        return patientService.update(id, command);
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
        return patientService.changePassword(id, command.password());
    }

    @Operation(
            summary = "Delete patient with given id",
            description = "Only a patient without visits can be deleted. The account is deleted with the patient."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Patient and account deleted"),
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
                    )),
            @ApiResponse(
                    responseCode = "409",
                    description = "The patient has visits, so it cannot be deleted",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@PathVariable long id) {
        patientService.deleteById(id);
    }
}
