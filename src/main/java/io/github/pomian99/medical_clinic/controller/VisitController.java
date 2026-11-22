package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.dto.BookingSummaryDto;
import io.github.pomian99.medical_clinic.dto.PageResponse;
import io.github.pomian99.medical_clinic.dto.VisitCreateCommand;
import io.github.pomian99.medical_clinic.dto.VisitDto;
import io.github.pomian99.medical_clinic.facade.VisitBookingFacade;
import io.github.pomian99.medical_clinic.service.VisitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Visits", description = "Medical clinic visits: create, book, list by doctor or patient")
@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {
    private final VisitService visitService;
    private final VisitBookingFacade bookingFacade;

    @Operation(
        summary = "List all visits page by page, or all visits of one doctor",
        description = "One path with two variants, chosen by the query parameters:\n\n"
            + "- without `doctorId`: one page of all visits. `page` counts from 0, `size` is at most 100, "
            + "`sort` takes startsAt, endsAt or id, e.g. `?page=1&size=5&sort=startsAt,desc`. "
            + "Without them: first page of 20 visits, sorted by startsAt, then id; "
            + "id is always added as the last sort key;\n"
            + "- with `doctorId`: all visits of this doctor, free slots and booked ones, from the earliest, "
            + "as a plain list (not a page; `page`, `size` and `sort` are ignored). "
            + "A doctor without visits, or an unknown doctor id, gives an empty list.",
        parameters = @Parameter(
            name = "doctorId",
            in = ParameterIn.QUERY,
            required = false,
            description = "Id of the doctor; the response is a plain list of their visits instead of a page",
            schema = @Schema(type = "integer", format = "int64")
        )
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "A page of visits, or a plain list of VisitDto when doctorId is given"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "doctorId is not a number, or sort uses a property other than startsAt, endsAt or id",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class)
            ))
    })
    @GetMapping
    public PageResponse<VisitDto> getVisits(
        @ParameterObject @PageableDefault(size = 20, sort = {"startsAt", "id"}) Pageable pageable
    ) {
        return PageResponse.from(visitService.findAll(pageable));
    }

    @Operation(hidden = true)
    @GetMapping(params = "doctorId")
    public List<VisitDto> getVisitsByDoctorId(@RequestParam long doctorId) {
        return visitService.findAllByDoctorId(doctorId);
    }

    @Operation(
            summary = "List visits of patient with given id",
            description = "Returns visits booked by the patient, from the earliest. "
                    + "A patient without visits gives an empty list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Visits of the patient"),
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
    @GetMapping("/patient/{patientId}")
    public List<VisitDto> getVisitsByPatientId(@PathVariable long patientId) {
        return visitService.findAllByPatientId(patientId);
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
        return visitService.create(command);
    }

    @Operation(
            summary = "Book visit with given id for patient with given id",
            description = "Assigns the patient to a free slot that has not started yet."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Visit booked, booking summary returned"),
            @ApiResponse(
                    responseCode = "400",
                    description = "The visit is in the past, or an id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No visit or no patient with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "409",
                    description = "The visit is already booked",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PatchMapping("/{id}/book/{patientId}")
    public BookingSummaryDto bookVisit(@PathVariable long id, @PathVariable long patientId) {
        return bookingFacade.book(id, patientId);
    }
}
