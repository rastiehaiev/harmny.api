package io.harmny.api.utils

import arrow.core.Either
import io.harmny.api.model.Context
import org.springframework.data.mongodb.core.query.Criteria

inline fun <A, B> Either<A, B>.fix(func: (A) -> B): B {
    return this.fold(func) { it }
}

fun Context.toBaseCriteria(): Criteria {
    val criteria = Criteria.where("userId").`is`(this.userId)
    val applicationId = this.applicationId
    return if (applicationId == null) {
        criteria
    } else {
        criteria.and("applicationId").`is`(applicationId)
    }
}

fun Criteria.letIf(condition: () -> Boolean, action: (Criteria) -> Criteria): Criteria {
    return if (condition()) {
        action(this)
    } else {
        this
    }
}

fun Criteria.letIf(condition: Boolean, action: (Criteria) -> Criteria): Criteria {
    return if (condition) {
        action(this)
    } else {
        this
    }
}
