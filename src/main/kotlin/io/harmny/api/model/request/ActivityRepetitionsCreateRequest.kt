package io.harmny.api.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ActivityRepetitionsCreateRequest(
    val timeSpentMs: String?,
    val startedAt: String?,
    val count: String?,
    val caloriesBurnt: String?,
    val distance: String?,
    val heartRate: String?,
    val mood: String?,
    val painLevel: String?,
    val complexity: String?,
    val started: String?,
)
