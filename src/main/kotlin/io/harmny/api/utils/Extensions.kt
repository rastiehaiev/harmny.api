package io.harmny.api.utils

import arrow.core.Either

inline fun <A, B> Either<A, B>.fix(func: (A) -> B): B {
    return this.fold(func) { it }
}
