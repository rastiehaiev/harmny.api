package io.harmny.api.endpoint

import arrow.core.flatMap
import io.harmny.api.instruments.ContextProvider
import io.harmny.api.model.asResponse
import io.harmny.api.model.request.ActivitiesCreateRequest
import io.harmny.api.model.request.ActivitiesListRequest
import io.harmny.api.model.request.ActivitiesUpdateRequest
import io.harmny.api.service.ActivitiesService
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivitiesEndpoint(
    private val contextProvider: ContextProvider,
    private val activitiesService: ActivitiesService,
) {

    @Operation(summary = "Create activity.")
    @PostMapping(path = ["/activities"])
    suspend fun createActivity(
        @RequestHeader headers: HttpHeaders,
        @RequestBody request: ActivitiesCreateRequest,
    ): ResponseEntity<out Any> {
        return contextProvider.provide(headers).flatMap { context ->
            withContext(Dispatchers.IO) {
                activitiesService.create(context, request)
            }
        }.fold(
            { fail -> fail.asResponse() },
            { activity -> ResponseEntity.status(HttpStatus.CREATED).body(activity) },
        )
    }

    @Operation(summary = "List activities.")
    @GetMapping(path = ["/activities"])
    suspend fun listActivities(
        @RequestHeader headers: HttpHeaders,
        request: ActivitiesListRequest,
    ): ResponseEntity<out Any> {
        return contextProvider.provide(headers).map { context ->
            withContext(Dispatchers.IO) {
                activitiesService.list(context)
            }
        }.fold(
            { fail -> fail.asResponse() },
            { activities -> ResponseEntity.ok(activities) },
        )
    }

    @Operation(summary = "Update activity.")
    @PutMapping(path = ["/activities/{activity_id}"])
    suspend fun updateActivity(
        @RequestHeader headers: HttpHeaders,
        @PathVariable("activity_id") activityId: String,
        @RequestBody request: ActivitiesUpdateRequest,
    ): ResponseEntity<out Any> {
        return contextProvider.provide(headers).flatMap { context ->
            withContext(Dispatchers.IO) {
                activitiesService.update(context, activityId, request)
            }
        }.fold(
            { fail -> fail.asResponse() },
            { activity -> ResponseEntity.ok(activity) },
        )
    }

    @Operation(summary = "Delete activity.")
    @DeleteMapping(path = ["/activities/{activity_id}"])
    suspend fun updateActivity(
        @RequestHeader headers: HttpHeaders,
        @PathVariable("activity_id") activityId: String,
    ): ResponseEntity<out Any> {
        return contextProvider.provide(headers).flatMap { context ->
            withContext(Dispatchers.IO) {
                activitiesService.delete(context, activityId)
            }
        }.fold(
            { fail -> fail.asResponse() },
            { activity -> ResponseEntity.ok(activity) },
        )
    }
}
