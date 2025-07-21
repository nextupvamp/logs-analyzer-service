package ru.nextupvamp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(info = @Info(
        title = "Logs analyzer API",
        version = "dev",
        contact = @Contact(
                name = "Vladislav Kulikov",
                url = "https://github.com/nextupvamp"
        )
))
public class SwaggerConfig {
}
