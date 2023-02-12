package io.harmny.api.service

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.right
import io.harmny.api.entity.BookEntity
import io.harmny.api.model.Book
import io.harmny.api.model.BookStatus
import io.harmny.api.model.Context
import io.harmny.api.model.Fail
import io.harmny.api.model.Page
import io.harmny.api.model.request.BooksCreateRequest
import io.harmny.api.model.request.BooksListRequest
import io.harmny.api.model.request.BooksUpdateRequest
import io.harmny.api.repository.BooksRepository
import io.harmny.api.utils.letIf
import io.harmny.api.utils.toBaseCriteria
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class BooksService(
    private val booksRepository: BooksRepository,
    private val mongoOperations: ReactiveMongoOperations,
) {

    private val maxPageSize = 50
    private val defaultPageSize = 20

    suspend fun list(context: Context, request: BooksListRequest): Either<Fail, Page<Book>> {
        return either {
            val bookStatus = parseBookStatus(request.status).bind()
            val pageNumber = parsePageNumber(request.pageNumber).bind()
            val pageSize = parsePageSize(request.pageSize).bind()
            val genres = request.genres.orEmpty()
            val authors = request.authors.orEmpty()

            val criteria = context.toBaseCriteria()
                .letIf(genres.isNotEmpty()) { it.and("genre").`in`(request.genres.orEmpty()) }
                .letIf(authors.isNotEmpty()) { it.and("author").`in`(request.authors.orEmpty()) }
                .letIf(bookStatus != null) { it.and("status").`is`(bookStatus) }

            val sort = getSorting(request).bind()
            val query = Query.query(criteria).with(PageRequest.of(pageNumber, pageSize, sort))

            val total = mongoOperations.count(query, BookEntity::class.java).awaitSingle()
            val books = if (total == 0L) emptyList() else mongoOperations.find(query, BookEntity::class.java)
                .collectList()
                .awaitSingle()
                .map { it.toBook() }
            Page(total = total, pageNumber = pageNumber, pageSize = pageSize, items = books)
        }
    }

    suspend fun create(context: Context, request: BooksCreateRequest): Either<Fail, Book> {
        return either {
            val name = validateName(request.name).bind()
            val author = validateAuthor(request.author).bind()
            val genre = validateGenre(request.genre).bind()
            val pagesCount = validatePagesCount(request.pagesCount).bind()

            val currentTime = Instant.now()
            val bookEntity = BookEntity(
                id = UUID.randomUUID().toString(),
                userId = context.userId,
                applicationId = context.applicationId,
                createdAt = currentTime,
                lastUpdatedAt = currentTime,
                name = name,
                author = author,
                genre = genre,
                status = BookStatus.NOT_STARTED.key,
                pagesCount = pagesCount,
            )
            booksRepository.save(bookEntity).awaitSingle().toBook()
        }
    }

    suspend fun update(context: Context, bookId: String, request: BooksUpdateRequest): Either<Fail, Book> {
        return either {
            val bookEntity = findById(context, bookId).bind()
            val bookEntityCopy = bookEntity.copy()
            request.author?.let { validateAuthor(it).bind() }?.also { bookEntity.author = it }
            request.name?.let { validateName(it).bind() }?.also { bookEntity.name = it }
            request.genre?.let { validateGenre(it).bind() }?.also { bookEntity.genre = it }
            request.pagesCount?.let { validatePagesCount(it).bind() }?.also { bookEntity.pagesCount = it }
            request.currentPageNumber?.let { parsePageNumber(it).bind() }?.also { bookEntity.currentPageNumber = it }
            if (request.started == true) with(bookEntity) {
                if (finishedAt == null) {
                    startedAt = Instant.now()
                    status = BookStatus.IN_PROGRESS.key
                }
            }
            if (request.finished == true) with(bookEntity) {
                val now = Instant.now()
                if (startedAt == null) {
                    startedAt = now.minus(7, ChronoUnit.DAYS)
                }
                finishedAt = now
                status = BookStatus.DONE.key
            }
            if (bookEntity != bookEntityCopy) {
                bookEntity.lastUpdatedAt = Instant.now()
                booksRepository.save(bookEntity).awaitSingle()
            }
            bookEntity.toBook()
        }
    }

    suspend fun delete(context: Context, bookId: String): Either<Fail, Book> {
        return findById(context, bookId).map { bookEntity ->
            booksRepository.deleteById(bookId).awaitSingle()
            bookEntity.toBook()
        }
    }

    private suspend fun findById(context: Context, bookId: String): Either<Fail, BookEntity> {
        val book = booksRepository.findById(bookId)
            .awaitSingleOrNull()
            ?.takeIf { it.userId == context.userId }
            ?.takeIf { context.applicationId == null || it.applicationId == context.applicationId }
            ?: return Fail.resource("book.not.found")
        return book.right()
    }

    private fun getSorting(request: BooksListRequest): Either<Fail, Sort> {
        val sortBy = request.sortBy.let {
            when (it) {
                null, "created" -> "createdAt"
                "updated" -> "lastUpdatedAt"
                "status" -> "status"
                else -> return Fail.input(
                    key = "sort.by.invalid",
                    properties = mapOf(
                        "SUPPORTED_VALUES" to listOf("created", "updated", "status")
                    ),
                )
            }
        }
        val sortDirection = request.sortDirection.let {
            when {
                it == null || it.equals("desc", ignoreCase = true) -> Direction.DESC
                it.equals("asc", ignoreCase = true) -> Direction.ASC
                else -> return Fail.input(
                    key = "sort.direction.invalid",
                    properties = mapOf(
                        "SUPPORTED_VALUES" to listOf("asc", "desc")
                    ),
                )
            }
        }

        return Sort.by(sortDirection, sortBy)
            .let { if (sortBy == "status") it.and(Sort.by(Direction.DESC, "lastUpdatedAt")) else it }
            .right()
    }

    private fun parsePageNumber(pageNumberString: String?): Either<Fail, Int> {
        return if (pageNumberString.isNullOrBlank()) {
            Either.Right(0)
        } else {
            val pageNumber = pageNumberString.toIntOrNull()
            if (pageNumber == null || pageNumber < 0) {
                Fail.input("page.number.invalid")
            } else {
                Either.Right(pageNumber)
            }
        }
    }

    private fun parsePageSize(pageSizeString: String?): Either<Fail, Int> {
        return if (pageSizeString.isNullOrBlank()) {
            Either.Right(defaultPageSize)
        } else {
            val pageSize = pageSizeString.toIntOrNull()
            if (pageSize == null || pageSize < 0) {
                Fail.input("page.size.invalid")
            } else {
                Either.Right(minOf(maxPageSize, pageSize))
            }
        }
    }

    private fun parseBookStatus(bookStatusString: String?): Either<Fail, BookStatus?> {
        return if (bookStatusString == null) {
            Either.Right(null)
        } else {
            BookStatus.of(bookStatusString)?.right() ?: Fail.input(
                key = "book.status.invalid",
                properties = mapOf(
                    "SUPPORTED_VALUES" to BookStatus.values(),
                )
            )
        }
    }

    private fun validateName(name: String?): Either<Fail, String> {
        return if (name.isNullOrBlank()) {
            Fail.input("book.name.empty")
        } else if (name.length > 100) {
            Fail.input("book.name.too.long")
        } else {
            name.trim().right()
        }
    }

    private fun validateAuthor(author: String?): Either<Fail, String> {
        return if (author.isNullOrBlank()) {
            Fail.input("book.author.empty")
        } else if (author.length > 60) {
            Fail.input("book.author.too.long")
        } else {
            author.trim().right()
        }
    }

    private fun validateGenre(genre: String?): Either<Fail, String> {
        return if (genre.isNullOrBlank()) {
            Fail.input("book.genre.empty")
        } else if (genre.length > 30) {
            Fail.input("book.genre.too.long")
        } else {
            genre.trim().lowercase().right()
        }
    }

    private fun validatePagesCount(pagesCount: String?): Either<Fail, Int?> {
        return if (pagesCount == null) {
            null.right()
        } else {
            pagesCount.trim().toIntOrNull()?.takeIf { it in 1..99999 }?.right()
                ?: Fail.input("books.pages.count.invalid")
        }
    }

    private fun BookEntity.toBook(): Book {
        return Book(
            id,
            name,
            author,
            genre,
            BookStatus.byKey(status),
            pagesCount,
            currentPageNumber,
            lastUpdatedAt,
            startedAt,
            finishedAt,
        )
    }
}
