package io.harmny.api.model.response

import com.fasterxml.jackson.annotation.JsonInclude

data class ErrorResponse(
    val failure: ErrorObject,
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class ErrorObject(
    val type: String,
    val description: String? = null,
    val properties: Map<String, Any>? = null,
)
