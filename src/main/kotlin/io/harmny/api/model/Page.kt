package io.harmny.api.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonPropertyOrder(value = ["total", "total_on_page", "page_number", "page_size", "items"])
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Page<T>(
    val total: Long,
    val pageNumber: Int,
    val pageSize: Int,
    val items: List<T>,
) {

    companion object {
        fun <T> empty(pageNumber: Int, pageSize: Int): Page<T> {
            return Page(total = 0, pageNumber, pageSize, emptyList())
        }
    }

    val totalOnPage: Int = items.size
}
