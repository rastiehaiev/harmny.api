package io.harmny.api.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Collections
import java.util.Enumeration
import java.util.Locale
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse

@Configuration
class WebConfig {

    @Bean
    fun requestParamsConverter(): Filter = SnakeCaseToCamelCaseFilter()
}

private class SnakeCaseToCamelCaseFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val formattedParams = HashMap<String, Array<String>>()

        for (param in request.parameterMap.keys) {
            formattedParams[param.toCamelCase()] = request.getParameterValues(param)
        }

        filterChain.doFilter(object : HttpServletRequestWrapper(request) {
            override fun getParameterMap(): MutableMap<String, Array<String>> = formattedParams
            override fun getParameterNames(): Enumeration<String> = Collections.enumeration(formattedParams.keys)
            override fun getParameterValues(name: String): Array<String>? = formattedParams[name]

            override fun getParameter(name: String?): String? = if (formattedParams.containsKey(name)) {
                formattedParams[name]!![0]
            } else null
        }, response)
    }

    private fun String.toCamelCase(): String {
        if (!this.contains('_')) {
            return this
        }
        val words = this.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val builder = StringBuilder(words[0])
        for (i in 1 until words.size) {
            builder.append(words[i].substring(0, 1).uppercase(Locale.getDefault()) + words[i].substring(1))
        }
        return builder.toString()
    }
}
