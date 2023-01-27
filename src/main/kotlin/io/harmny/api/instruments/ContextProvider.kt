package io.harmny.api.instruments

import arrow.core.Either
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.harmny.api.model.Context
import io.harmny.api.model.Fail
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

@Component
class ContextProvider(
    private val objectMapper: ObjectMapper,
) {

    private val rawKey: String = "DAVLtjoTHQ3uhsGm2VstWj5M2JsdhhxQPy71BL11XQ4E5OpRgfCYjNPELkP1M6g"
    private val key = Keys.hmacShaKeyFor(rawKey.toByteArray())
    private val parser = Jwts.parserBuilder().setSigningKey(key).build()

    fun provide(headers: HttpHeaders): Either<Fail, Context> {
        val token = headers.getFirst("Authorization")
            ?.split(" ")
            ?.takeIf { it.size == 2 }
            ?.takeIf { it[0].equals("Bearer", ignoreCase = true) }
            ?.get(1)
            ?: return Fail.authentication("token.invalid")

        return parseToken(token).map { Context(it.userId, it.applicationId) }
    }

    private fun parseToken(token: String): Either<Fail, TokenCompact> {
        return try {
            val claims = parser.parseClaimsJws(token).body
            objectMapper.readValue<TokenCompact>(claims.get("token", String::class.java)).right()
        } catch (e: Exception) {
            Fail.authorization(key = "token.invalid")
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
private data class TokenCompact(
    @JsonProperty("u")
    val userId: String,
    @JsonProperty("a")
    val applicationId: String? = null,
)
