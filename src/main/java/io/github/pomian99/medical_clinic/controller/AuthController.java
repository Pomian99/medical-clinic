package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.dto.LoginCommand;
import io.github.pomian99.medical_clinic.dto.TokenDto;
import io.github.pomian99.medical_clinic.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Logging in: an access token for the e-mail and password of an account")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final TokenService tokenService;

    @Operation(summary = "Log in and get an access token valid for one hour")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token issued"),
        @ApiResponse(
            responseCode = "400",
            description = "Request body failed validation or is not valid JSON",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class)
            )),
        @ApiResponse(
            responseCode = "401",
            description = "Wrong e-mail or password",
            content = @Content(
                schema = @Schema(implementation = ProblemDetail.class)
            ))
    })
    @PostMapping("/token")
    public TokenDto createToken(@Valid @RequestBody LoginCommand command) {
        return tokenService.issue(command);
    }
}
