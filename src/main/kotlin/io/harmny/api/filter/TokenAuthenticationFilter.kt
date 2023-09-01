package io.harmny.api.filter

import arrow.core.Either
import arrow.core.right
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.harmny.api.model.Context
import io.harmny.api.model.Fail
import io.harmny.api.model.Fails
import io.harmny.api.model.asResponse
import io.harmny.api.properties.HarmnyProperties
import io.harmny.api.utils.fix
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TokenAuthenticationFilter(
    private val objectMapper: ObjectMapper,
    properties: HarmnyProperties,
) : OncePerRequestFilter() {

    companion object {
        private val log = LoggerFactory.getLogger(TokenAuthenticationFilter::class.java)
    }

    private val key = Keys.hmacShaKeyFor(properties.auth.tokenSecret.toByteArray())
    private val parser = Jwts.parserBuilder().setSigningKey(key).build()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val tokenString = getToken(request)
        if (tokenString != null) {
            parseToken(tokenString).map {
                val context = Context(it.userId, it.applicationId)
                val authentication = TokenPrincipalAuthentication(context)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
                filterChain.doFilter(request, response)
            }.fix { fail ->
                response.sendError(fail)
            }
        } else {
            response.sendError(Fails.authentication("token.missing"))
        }
    }

    private fun HttpServletResponse.sendError(fail: Fail) {
        val responseEntity = fail.asResponse()
        val body = responseEntity.body.let { objectMapper.writeValueAsString(it) }

        addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        status = responseEntity.statusCodeValue
        writer.write(body)
        writer.flush()
    }

    private fun getToken(request: HttpServletRequest): String? {
        return request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith("Bearer ") }
            ?.split(" ")
            ?.takeIf { it.size == 2 }
            ?.takeIf { it[0].equals("Bearer", ignoreCase = true) }
            ?.get(1)
            ?.trim()
    }

    private fun parseToken(token: String): Either<Fail, TokenCompact> {
        return try {
            val claimsJws = parser.parseClaimsJws(token)
            objectMapper.readValue<TokenCompact>(claimsJws.body.subject).right()
        } catch (e: Exception) {
            log.error("Failed to parse token. Reason: ${e.message}")
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

private class TokenPrincipalAuthentication(
    private val context: Context,
) : AbstractAuthenticationToken(emptyList()) {
    override fun getPrincipal() = context
    override fun getCredentials() = context.userId
    override fun isAuthenticated() = true
}
