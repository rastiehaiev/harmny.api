package io.harmny.api.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RoutinesCreateRequest(
    val name: String?,
    val items: List<RoutineCreateRequestActivityItems>?,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RoutineCreateRequestActivityItems(
    val activityId: String?,
    val note: String?,
)
