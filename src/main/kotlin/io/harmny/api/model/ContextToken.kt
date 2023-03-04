package io.harmny.api.model

import org.springframework.security.authentication.AbstractAuthenticationToken

class ContextToken(val context: Context) : AbstractAuthenticationToken(emptyList()) {
    override fun getCredentials(): Any = context
    override fun getPrincipal(): Any = context
}
