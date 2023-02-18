package io.harmny.api.model

import arrow.core.Either
import arrow.core.left
import io.harmny.api.model.response.ErrorObject
import io.harmny.api.model.response.ErrorResponse
import org.springframework.http.ResponseEntity

data class Fail(
    val type: String,
    val description: String?,
    val properties: Map<String, Any>?,
) {

    companion object {

        fun <S> input(
            key: String = "",
            description: String? = null,
            properties: Map<String, Any>? = null,
        ): Either<Fail, S> = Fails.input(key, description, properties).left()

        fun <S> resource(
            key: String = "",
            description: String? = null,
            properties: Map<String, Any>? = null,
        ): Either<Fail, S> = Fails.resource(key, description, properties).left()

        fun <S> authentication(
            key: String = "",
            description: String? = null,
            properties: Map<String, Any>? = null,
        ): Either<Fail, S> = Fails.authentication(key, description, properties).left()

        fun <S> authorization(
            key: String = "",
            description: String? = null,
            properties: Map<String, Any>? = null,
        ): Either<Fail, S> = Fails.authorization(key, description, properties).left()

        fun <S> conflict(
            key: String = "",
            description: String? = null,
            properties: Map<String, Any>? = null,
        ): Either<Fail, S> = Fails.conflict(key, description, properties).left()

        fun <S> internal(
            key: String = "",
            description: String? = null,
            properties: Map<String, Any>? = null,
        ): Either<Fail, S> = Fails.internal(key, description, properties).left()
    }
}

object Fails {

    fun input(
        key: String = "",
        description: String? = null,
        properties: Map<String, Any>? = null,
    ): Fail {
        return general("fail.input", key, description, properties)
    }

    fun resource(
        key: String = "",
        description: String? = null,
        properties: Map<String, Any>? = null,
    ): Fail {
        return general("fail.resource", key, description, properties)
    }

    fun authentication(
        key: String = "",
        description: String? = null,
        properties: Map<String, Any>? = null,
    ): Fail {
        return general("fail.authentication", key, description, properties)
    }

    fun authorization(
        key: String = "",
        description: String? = null,
        properties: Map<String, Any>? = null,
    ): Fail {
        return general("fail.authorization", key, description, properties)
    }

    fun conflict(
        key: String = "",
        description: String? = null,
        properties: Map<String, Any>? = null,
    ): Fail {
        return general("fail.conflict", key, description, properties)
    }

    fun internal(
        key: String = "",
        description: String? = null,
        properties: Map<String, Any>? = null,
    ): Fail {
        return general("fail.internal", key, description, properties)
    }

    private fun general(
        type: String,
        key: String,
        description: String? = null,
        properties: Map<String, Any>? = null,
    ): Fail {
        val resultType = if (key.isNotBlank()) "$type.$key" else type
        return Fail(resultType, description, properties)
    }
}

fun Fail.asResponse(): ResponseEntity<ErrorResponse> {
    val statusCode = when {
        this.type.startsWith("fail.resource") -> 404
        this.type.startsWith("fail.authentication") -> 401
        this.type.startsWith("fail.authorization") -> 403
        this.type.startsWith("fail.conflict") -> 409
        this.type.startsWith("fail.internal") -> 500
        else -> 400
    }
    return ResponseEntity.status(statusCode).body(ErrorResponse(ErrorObject(type, description, properties)))
}
