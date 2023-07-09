package io.harmny.api.utils

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
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

fun String.validateNumber(
    key: String,
    minValue: Int,
    maxValue: Int,
): Either<Fail, Int> {
    return this.toIntOrNull()
        ?.takeIf { it > minValue }
        ?.takeIf { it < maxValue }
        ?.right()
        ?: return Fail.input(
            key = key,
            properties = mapOf(
                "min_value" to minValue,
                "max_value" to maxValue,
            )
        )
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
    minLength: Int = 4,
    maxLength: Int,
): Either<Fail, String> {
    val nameNormalised = trim().takeIf { it.isNotBlank() } ?: return Fail.input("$key.blank")
    return nameNormalised.takeIf { it.matches(nameRegex) }
        .rightIfNotNull {
            Fails.input(
                key = "$key.invalid.pattern.mismatch",
                properties = mapOf("supported_pattern" to NAME_PATTERN),
            )
        }.flatMap { resultName ->
            resultName.takeIf { it.length <= maxLength }
                .rightIfNotNull {
                    Fails.input(
                        key = "$key.invalid.too.long",
                        properties = mapOf("max_length" to maxLength),
                    )
                }
        }.flatMap { resultName ->
            resultName.takeIf { it.length >= minLength }
                .rightIfNotNull {
                    Fails.input(
                        key = "$key.invalid.too.short",
                        properties = mapOf("min_length" to minLength),
                    )
                }
        }
}
