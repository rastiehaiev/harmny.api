package io.harmny.api.endpoint

import io.harmny.api.model.response.ErrorObject
import io.harmny.api.model.response.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.HttpClientErrorException.NotFound
import org.springframework.web.servlet.NoHandlerFoundException

@ControllerAdvice
class ExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(ExceptionHandler::class.java)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    fun on404(e: NoHandlerFoundException): ErrorResponse {
        log.error("Failed to process request. No handler for request (${e.httpMethod} ${e.requestURL}).")
        return ErrorResponse(ErrorObject(type = "fail.error.not.found"))
    }

    @ExceptionHandler(NotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    fun on404(e: NotFound): ErrorResponse {
        log.error("Failed to process request. No handler for request.")
        return ErrorResponse(ErrorObject(type = "fail.error.not.found"))
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun on500(e: Exception): ErrorResponse {
        log.error("Failed to process request. Reason: ${e.message}", e)
        return ErrorResponse(ErrorObject(type = "fail.error.internal"))
    }
}
