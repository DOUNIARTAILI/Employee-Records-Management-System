package com.drtaili.security.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Dounia Rtaili",
                        email = "rtailidounia@gmail.com",
                        url = "https://github.com/DOUNIARTAILI"
                ),
                description = "OpenApi documentation for Employee Records Management System",
                title = "OpenApi - drtaili",
                version = "1.0"
        ),
        servers = {
                @Server(
                        description = "Local Env",
                        url = "http://localhost:8080"
                )
        }
)
public class OpenApiConfig {
}
