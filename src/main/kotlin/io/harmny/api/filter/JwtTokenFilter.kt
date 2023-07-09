package io.harmny.api.filter

import arrow.core.Either
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.harmny.api.model.Context
import io.harmny.api.model.ContextToken
import io.harmny.api.model.Fail
import io.harmny.api.properties.HarmnyProperties
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JwtTokenFilter(
    private val objectMapper: ObjectMapper,
    properties: HarmnyProperties,
) : OncePerRequestFilter() {

    private val key = Keys.hmacShaKeyFor(properties.jwtKey.toByteArray())
    private val parser = Jwts.parserBuilder().setSigningKey(key).build()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith("Bearer ") }
            ?.split(" ")
            ?.takeIf { it.size == 2 }
            ?.takeIf { it[0].equals("Bearer", ignoreCase = true) }
            ?.get(1)
            ?.trim()
            ?.let { token ->
                parseToken(token).map {
                    val context = Context(it.userId, it.applicationId)
                    SecurityContextHolder.getContext().authentication = ContextToken(context)
                }
            }

        filterChain.doFilter(request, response)
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
