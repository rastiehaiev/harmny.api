package io.harmny.api.repository

import io.harmny.api.entity.BookEntity
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BooksRepository : ReactiveMongoRepository<BookEntity, String>
