package io.harmny.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Book(
    val id: String,
    val name: String,
    val author: String,
    val genre: String,
    val status: BookStatus = BookStatus.NOT_STARTED,
    val pagesCount: Int? = null,
    val currentPageNumber: Int? = null,
    val lastUpdatedAt: Instant,
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
)

enum class BookStatus(val key: String) {

    NOT_STARTED("0_ns"),
    IN_PROGRESS("1_ip"),
    DONE("2_d");

    companion object {
        fun of(value: String): BookStatus? {
            return BookStatus.values().firstOrNull { it.name.equals(value, ignoreCase = true) }
        }

        fun byKey(key: String): BookStatus {
            return BookStatus.values().first { it.key == key }
        }
    }
}
