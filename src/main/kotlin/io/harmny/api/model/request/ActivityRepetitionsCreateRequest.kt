package io.harmny.api.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ActivityRepetitionsCreateRequest(
    val timeSpentMs: String?,
    val count: String?,
    val caloriesBurnt: String?,
    val completed: String?,
)
