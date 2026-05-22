package com.diceduel.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Dice Duel API",
                version = "1.1.0",
                description = "REST API for a multiplayer dice duel game."
        )
)
public class OpenApiConfig {
}
