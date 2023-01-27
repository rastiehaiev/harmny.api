package io.harmny.api.endpoint

import arrow.core.flatMap
import io.harmny.api.instruments.ContextProvider
import io.harmny.api.model.asResponse
import io.harmny.api.response.SimpleListResponse
import io.harmny.api.service.BooksService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class BooksEndpoint(
    private val booksService: BooksService,
    private val contextProvider: ContextProvider,
) {

    @Operation(summary = "List books.")
    @GetMapping(path = ["/books"])
    fun listBooks(
        @RequestHeader headers: HttpHeaders,
    ): ResponseEntity<out Any> {
        return contextProvider.provide(headers)
            .flatMap { booksService.getBooks(it) }
            .fold(
                { fail -> fail.asResponse() },
                { books -> ResponseEntity.ok(SimpleListResponse(books)) },
            )
    }
}
