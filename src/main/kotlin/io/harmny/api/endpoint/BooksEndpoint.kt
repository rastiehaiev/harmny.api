package io.harmny.api.endpoint

import arrow.core.flatMap
import io.harmny.api.instruments.ContextProvider
import io.harmny.api.model.asResponse
import io.harmny.api.model.request.BooksCreateRequest
import io.harmny.api.model.request.BooksListRequest
import io.harmny.api.model.request.BooksUpdateRequest
import io.harmny.api.service.BooksService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class BooksEndpoint(
    private val booksService: BooksService,
    private val contextProvider: ContextProvider,
) {

    @Operation(summary = "Create book.")
    @PostMapping(path = ["/books"])
    suspend fun createBook(
        @RequestHeader headers: HttpHeaders,
        @RequestBody request: BooksCreateRequest,
    ): ResponseEntity<out Any> {
        return contextProvider.provide(headers).flatMap { context ->
            booksService.create(context, request)
        }.fold(
            { fail -> fail.asResponse() },
            { book -> ResponseEntity.status(HttpStatus.CREATED).body(book) },
        )
    }

    @Operation(summary = "List books.")
    @GetMapping(path = ["/books"])
    suspend fun listBooks(
        @RequestHeader headers: HttpHeaders,
        request: BooksListRequest,
    ): ResponseEntity<out Any> {
        return contextProvider.provide(headers).flatMap { context ->
            booksService.list(context, request)
        }.fold(
            { fail -> fail.asResponse() },
            { page -> ResponseEntity.ok(page) },
        )
    }

    @Operation(summary = "Update book.")
    @PutMapping(path = ["/books/{book_id}"])
    suspend fun updateBook(
        @RequestHeader headers: HttpHeaders,
        @PathVariable("book_id") bookId: String,
        @RequestBody request: BooksUpdateRequest,
    ): ResponseEntity<out Any> {
        return contextProvider.provide(headers).flatMap { context ->
            booksService.update(context, bookId, request)
        }.fold(
            { fail -> fail.asResponse() },
            { book -> ResponseEntity.ok(book) },
        )
    }

    @Operation(summary = "Delete book.")
    @DeleteMapping(path = ["/books/{book_id}"])
    suspend fun deleteBook(
        @RequestHeader headers: HttpHeaders,
        @PathVariable("book_id") bookId: String,
    ): ResponseEntity<out Any> {
        return contextProvider.provide(headers).flatMap { context ->
            booksService.delete(context, bookId)
        }.fold(
            { fail -> fail.asResponse() },
            { book -> ResponseEntity.ok(book) },
        )
    }
}
