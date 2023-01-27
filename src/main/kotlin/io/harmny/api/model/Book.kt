package io.harmny.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Book(
    val name: String,
    val authors: List<String>,
    val genre: String,
    val status: BookStatus = BookStatus.NOT_STARTED,
    val pagesCount: Int? = null,
    val currentPageNumber: Int? = null,
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
)

enum class BookStatus {

    NOT_STARTED,
    IN_PROGRESS,
    DONE,
}
