package io.harmny.api.model

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Book(
    val name: String,
    val authors: List<String>,
    val genre: String,
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
)
