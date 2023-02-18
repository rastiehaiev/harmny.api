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
import io.harmny.api.utils.parsePageNumber
import io.harmny.api.utils.parsePageSize
import io.harmny.api.utils.validateName
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class BooksService(
    private val booksRepository: BooksRepository,
) {

    companion object {
        private const val maxPageSize = 50
        private const val defaultPageSize = 20
    }

    suspend fun list(context: Context, request: BooksListRequest): Either<Fail, Page<Book>> {
        return either {
            val bookStatus = parseBookStatus(request.status).bind()
            val pageNumber = parsePageNumber(request.pageNumber).bind()
            val pageSize = parsePageSize(request.pageSize).bind()
            val genres = request.genres.orEmpty()
            val authors = request.authors.orEmpty()

            /*val criteria = context.toBaseCriteria()
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
            Page(total = total, pageNumber = pageNumber, pageSize = pageSize, items = books)*/
            TODO()
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
            // booksRepository.save(bookEntity).awaitSingle().toBook()
            TODO()
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
                // TODO booksRepository.save(bookEntity).awaitSingle()
            }
            bookEntity.toBook()
        }
    }

    suspend fun delete(context: Context, bookId: String): Either<Fail, Book> {
        return findById(context, bookId).map { bookEntity ->
            // booksRepository.deleteById(bookId).awaitSingleOrNull()
            bookEntity.toBook()
        }
    }

    private suspend fun findById(context: Context, bookId: String): Either<Fail, BookEntity> {
        /*val book = booksRepository.findById(bookId)
            .awaitSingleOrNull()
            ?.takeIf { it.userId == context.userId }
            ?.takeIf { context.applicationId == null || it.applicationId == context.applicationId }
            ?: return Fail.resource("book.not.found")
        return book.right()*/
        TODO()
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

    private fun parsePageNumber(pageNumber: String?): Either<Fail, Int> {
        return pageNumber.parsePageNumber()
    }

    private fun parsePageSize(pageSize: String?): Either<Fail, Int> {
        return pageSize.parsePageSize(defaultPageSize, maxPageSize)
    }

    private fun validateName(name: String?): Either<Fail, String> {
        return (name ?: "").validateName("book.name", maxLength = 100).map { it.lowercase() }
    }

    private fun validateAuthor(author: String?): Either<Fail, String> {
        return (author ?: "").validateName("book.author", maxLength = 60).map { it.lowercase() }
    }

    private fun validateGenre(genre: String?): Either<Fail, String> {
        return (genre ?: "").validateName("book.genre", maxLength = 30).map { it.lowercase() }
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
