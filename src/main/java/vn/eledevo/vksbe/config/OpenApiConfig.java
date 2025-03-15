package vn.eledevo.vksbe.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(description = "OpenApi documentation for VKS", title = "VKS", version = "1.0"),
        servers = {
            @Server(description = "Local", url = "http://localhost:8081"),
            @Server(description = "Develop", url = "https://dev-vks.eledevo.com"),
            @Server(description = "Tester", url = "https://test-vks.eledevo.com"),
            @Server(description = "Staging", url = "https://uat-vks.eledevo.com")
        },
        security = {@SecurityRequirement(name = "bearerAuth")})
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {}
