package io.harmny.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ActivityRepetition(
    val id: String,
    val createdAt: Instant,
    val startedAt: Instant,
    var lastStartedAt: Instant?,
    val count: Int?,
    val caloriesBurnt: Int?,
    var started: Boolean? = null,
    var timeSpentMs: Int?,
    var heartRate: Int?,
    var mood: Int?,
    var painLevel: Int?,
    var complexity: Int?,
    var distance: Int?,
    var restarts: Int?,
    var completed: Boolean = true,
)
