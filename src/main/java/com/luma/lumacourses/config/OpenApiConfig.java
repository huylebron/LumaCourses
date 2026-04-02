package com.luma.lumacourses.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
// configure OpenAPI documentation
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "LumaCourses API",
                version = "v1",
                description = "REST API for LumaCourses e-learning platform"
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Enter JWT access token obtained from POST /api/auth/login"
)
public class OpenApiConfig {
}
