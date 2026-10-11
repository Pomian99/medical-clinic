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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Visits", description = "Medical clinic visits: create")
@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {
    private final VisitService visitService;
    private final VisitMapper mapper;

    @Operation(
            summary = "Create new visit",
            description = "Creates a free slot: a visit with a doctor and no patient yet."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Visit created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request body failed validation or is not valid JSON",
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
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitDto createVisit(@Valid @RequestBody VisitCreateCommand command) {
        return mapper.toDto(visitService.create(command));
    }
}
