package io.harmny.api.service

import arrow.core.Either
import arrow.core.right
import io.harmny.api.model.Book
import io.harmny.api.model.Context
import io.harmny.api.model.Fail
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class BooksService {

    fun getBooks(context: Context): Either<Fail, List<Book>> {
        return listOf(
            Book(
                name = "Kobzar",
                authors = listOf("Taras Shevchenko"),
                genre = "Poem",
                finishedAt = Instant.now(),
            )
        ).right()
    }
}
