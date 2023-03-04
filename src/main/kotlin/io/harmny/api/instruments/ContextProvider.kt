package io.harmny.api.instruments

import arrow.core.Either
import arrow.core.right
import io.harmny.api.model.Context
import io.harmny.api.model.ContextToken
import io.harmny.api.model.Fail
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class ContextProvider {

    fun provide(): Either<Fail, Context> {
        val contextToken = SecurityContextHolder.getContext().authentication as? ContextToken?
        return contextToken?.context?.right() ?: Fail.authentication("token.invalid")
    }
}
