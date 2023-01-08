package io.harmny.api.configuration

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI? {
        return OpenAPI().info(
            Info().title("Harmny API")
                .description("Harmny API application")
                .version("0.0.1")
        )
    }
}