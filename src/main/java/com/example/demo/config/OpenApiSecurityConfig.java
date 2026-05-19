package com.example.demo.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(security = @SecurityRequirement(name = "jwtCookie"))
@SecurityScheme(
        name = "jwtCookie",
        type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.APIKEY,
        in = io.swagger.v3.oas.annotations.enums.SecuritySchemeIn.COOKIE,
        paramName = "JWT_TOKEN"
)
@Configuration
public class OpenApiSecurityConfig {
}
