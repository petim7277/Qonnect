package com.example.qonnect;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "QONNECT API Documentation",
                description = "Qonnect creates a unified space for QA engineers, developers, and organizations to collaborate on bug tracking, resolution, and task management — with personalized dashboards and interactive, user-focused design.",
                version = "v1",
                contact = @Contact(
                        name = "Praise",
                        email = "praiseoyewole560@gmaill.com"
                )
        ),
        servers = {
                @Server(url = "${swagger.server.url}", description = "Blog API Server")
        },
        security = {
                @SecurityRequirement(name = "Keycloak")
        }
)
@SecurityScheme(
        name = "Keycloak",
        description = "JWT Authentication via Keycloak",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        openIdConnectUrl = "http://localhost:9090/realms/Qonnect/.well-known/openid-configuration"
)
@SpringBootApplication
public class QonnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(QonnectApplication.class, args);
    }
}