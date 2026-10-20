package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.dto.VisitCreateCommand;
import io.github.pomian99.medical_clinic.dto.VisitDto;
import io.github.pomian99.medical_clinic.mapper.VisitMapper;
import io.github.pomian99.medical_clinic.service.VisitService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Visits", description = "Medical clinic visits: create, list by doctor")
@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {
    private final VisitService visitService;
    private final VisitMapper mapper;

    @Operation(
            summary = "List visits of doctor with given id",
            description = "Returns both free slots and booked visits. "
                    + "A doctor without visits, or an unknown doctor id, gives an empty list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Visits of the doctor"),
            @ApiResponse(
                    responseCode = "400",
                    description = "The doctorId parameter is missing or is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping
    public List<VisitDto> getVisitsByDoctorId(@RequestParam long doctorId) {
        return mapper.toDto(visitService.findAllByDoctorId(doctorId));
    }

    @Operation(
            summary = "Create new visit",
            description = "Creates a free slot: a visit with a doctor and no patient yet."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Visit created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request body failed validation, is not valid JSON, "
                            + "or the slot is in the past or not made of whole 15-minute blocks",
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
                    description = "The doctor already has a visit overlapping this slot",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDto createVisit(@Valid @RequestBody VisitCreateCommand command) {
        return mapper.toDto(visitService.create(command));
    }
}
