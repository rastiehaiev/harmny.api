package io.harmny.api.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BooksUpdateRequest(
    val name: String?,
    val author: String?,
    val genre: String?,
    val pagesCount: String?,
    val currentPageNumber: String?,
    val started: Boolean?,
    val finished: Boolean?,
)
