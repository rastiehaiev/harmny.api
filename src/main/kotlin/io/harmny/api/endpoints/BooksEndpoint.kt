package io.harmny.api.endpoints

import io.harmny.api.model.Book
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class BooksEndpoint {

    @Operation(summary = "Returns the list of all available books.")
    @GetMapping(path = ["/books"])
    fun getAllBooks(): List<Book> {
        return listOf(
            Book(
                name = "Kobzar",
                authors = listOf("Taras Shevchenko"),
                genre = "Poem",
                finishedAt = Instant.now(),
            )
        )
    }
}
