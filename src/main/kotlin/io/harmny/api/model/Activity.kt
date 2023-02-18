package io.harmny.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Activity(
    val id: String,
    val name: String,
    val group: Boolean,
    val parentActivityId: String?,
    val createdAt: Instant,
    val lastUpdatedAt: Instant,
    val childActivities: List<Activity>?,
)
