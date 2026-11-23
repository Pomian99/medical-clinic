package io.github.pomian99.medical_clinic.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
@SecurityScheme(
    name = OpenApiConfig.BEARER_AUTH,
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Access token from POST /auth/token"
)
public class OpenApiConfig {
    static final String BEARER_AUTH = "bearerAuth";
}
