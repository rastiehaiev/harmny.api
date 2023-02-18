package io.harmny.api.entity

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "books")
data class BookEntity(
    @Id
    val id: String,
    @Column(name = "user_id")
    val userId: String,
    @Column(name = "application_id")
    val applicationId: String?,
    @Column(name = "created_at")
    val createdAt: Instant,
    @Column(name = "last_updated_at")
    var lastUpdatedAt: Instant,
    @Column(name = "name")
    var name: String,
    @Column(name = "author")
    var author: String,
    @Column(name = "genre")
    var genre: String,
    @Column(name = "status")
    var status: String,
    @Column(name = "pages_count")
    var pagesCount: Int? = null,
    @Column(name = "current_page_number")
    var currentPageNumber: Int? = null,
    @Column(name = "started_at")
    var startedAt: Instant? = null,
    @Column(name = "finished_at")
    var finishedAt: Instant? = null,
)
