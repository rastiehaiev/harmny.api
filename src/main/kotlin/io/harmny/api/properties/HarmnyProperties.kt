package io.harmny.api.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "harmny")
data class HarmnyProperties(
    val cors: Cors,
    val auth: Auth,
)

data class Cors(
    val allowedOrigins: String,
)

data class Auth(
    val tokenSecret: String,
)
