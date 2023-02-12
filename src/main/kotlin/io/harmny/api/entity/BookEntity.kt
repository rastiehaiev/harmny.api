package io.harmny.api.entity

import io.harmny.api.model.BookStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("books")
data class BookEntity(
    @Id
    val id: String,
    val userId: String,
    val applicationId: String?,
    val createdAt: Instant,
    var lastUpdatedAt: Instant,
    var name: String,
    var author: String,
    var genre: String,
    var status: String,
    var pagesCount: Int? = null,
    var currentPageNumber: Int? = null,
    var startedAt: Instant? = null,
    var finishedAt: Instant? = null,
)
