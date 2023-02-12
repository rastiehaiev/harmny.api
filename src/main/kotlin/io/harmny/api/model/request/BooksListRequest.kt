package io.harmny.api.model.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BooksListRequest(
    val pageNumber: String?,
    val pageSize: String?,
    val status: String?,
    val genres: List<String>?,
    val authors: List<String>?,
    val sortBy: String?,
    val sortDirection: String?,
)
