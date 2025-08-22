package com.donorbox.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    @Bean
    public OpenAPI donorboxOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl(baseUrl);
        localServer.setDescription("Server URL for Local Development");

        // Create security scheme for HTTP Basic Authentication
        SecurityScheme basicAuthScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")
                .name("basicAuth")
                .description("HTTP Basic Authentication. Use 'admin' as username and 'admin123' as password for admin endpoints.");

        // Create security requirement
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("basicAuth");

        Info info = new Info()
                .title("Donorbox Crowdfunding Platform API")
                .version("1.0")
                .description("API documentation for the Donorbox crowdfunding platform backend with international payment support.\n\n" +
                           "**Authentication:**\n" +
                           "- Public endpoints (under /api/public) do not require authentication\n" +
                           "- Admin endpoints (under /admin) require HTTP Basic Authentication\n" +
                           "- Username: `admin`\n" +
                           "- Password: `admin123`\n\n" +
                           "Click the 'Authorize' button below to authenticate for admin endpoints.");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("basicAuth", basicAuthScheme));
    }
}
