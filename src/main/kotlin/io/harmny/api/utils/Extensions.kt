package io.harmny.api.utils

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.rightIfNotNull
import io.harmny.api.model.Fail
import io.harmny.api.model.Fails

private val multipleSpacesRegex = "\\s+".toRegex()
private const val NAME_PATTERN = "[\\p{L}\\w\\s-_#]+"
private val nameRegex = NAME_PATTERN.toRegex()

inline fun <A, B> Either<A, B>.fix(func: (A) -> B): B {
    return this.fold(func) { it }
}

fun String.reduceRepeatedSpaces(): String {
    return replace(multipleSpacesRegex, " ")
}

fun String?.parsePageNumber(): Either<Fail, Int> {
    return if (this.isNullOrBlank()) {
        Either.Right(0)
    } else {
        val pageNumber = this.toIntOrNull()
        if (pageNumber == null || pageNumber < 0) {
            Fail.input("page.number.invalid")
        } else {
            Either.Right(pageNumber)
        }
    }
}

fun String?.parsePageSize(
    defaultPageSize: Int,
    maxPageSize: Int,
): Either<Fail, Int> {
    return if (this.isNullOrBlank()) {
        Either.Right(defaultPageSize)
    } else {
        val pageSize = this.toIntOrNull()
        if (pageSize == null || pageSize < 0) {
            Fail.input("page.size.invalid")
        } else {
            Either.Right(minOf(maxPageSize, pageSize))
        }
    }
}

fun String.validateName(
    key: String,
    maxLength: Int,
): Either<Fail, String> {
    val nameNormalised = trim().takeIf { it.isNotBlank() } ?: return Fail.input("$key.blank")
    return nameNormalised.takeIf { it.matches(nameRegex) }
        .rightIfNotNull {
            Fails.input(
                key = "$key.invalid",
                properties = mapOf("SUPPORTED_PATTERN" to NAME_PATTERN),
            )
        }.flatMap { resultTag ->
            resultTag.takeIf { it.length < maxLength }
                .rightIfNotNull {
                    Fails.input(
                        key = "$key.invalid",
                        properties = mapOf("MAX_SIZE" to maxLength),
                    )
                }
        }
}
