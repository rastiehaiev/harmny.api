package io.harmny.api.repository

import io.harmny.api.entity.BookEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BooksRepository : CrudRepository<BookEntity, String>
