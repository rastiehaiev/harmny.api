package io.harmny.api.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
class SecurityConfig {

    private val authWhiteList = arrayOf(
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/v2/api-docs/**",
        "/swagger-resources/**"
    )

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .csrf().disable()
            .authorizeHttpRequests { it.antMatchers(*authWhiteList).permitAll() }
            .build()
    }
}
