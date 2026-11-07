package io.github.pomian99.medical_clinic.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Tag(name = "Postman", description = "Request collection of this API in Postman format")
@RestController
@RequestMapping("/postman")
public class PostmanController {
    private static final String PATIENTS_COLLECTION = "medical-clinic.postman_collection.json";

    @Operation(summary = "Download the Postman collection of the API")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Postman collection returned"),
        @ApiResponse(
            responseCode = "500",
            description = "The collection file is missing from the classpath",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class)
            ))
    })
    @GetMapping(value = "/patients", produces = MediaType.APPLICATION_JSON_VALUE)
    public String patientsCollection() throws IOException {
        Resource collection = new ClassPathResource(PATIENTS_COLLECTION);
        try (InputStream in = collection.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
