package io.harmny.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "harmny")
data class HarmnyProperties(
    val jwtKey: String,
    val corsAllowedOrigins: String,
)
