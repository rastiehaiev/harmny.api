package io.harmny.api.model.request

data class BooksListRequest(
    val pageNumber: String?,
    val pageSize: String?,
    val status: String?,
    val genres: List<String>?,
    val authors: List<String>?,
    val sortBy: String?,
    val sortDirection: String?,
)
