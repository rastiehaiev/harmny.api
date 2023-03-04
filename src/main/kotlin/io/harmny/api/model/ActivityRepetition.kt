package io.harmny.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ActivityRepetition(
    val id: String,
    val timeSpent: Int?,
    val count: Int?,
    val caloriesBurnt: Int?,
    val time: Instant,
)
