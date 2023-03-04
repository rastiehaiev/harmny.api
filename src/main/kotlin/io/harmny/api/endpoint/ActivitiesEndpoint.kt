package io.harmny.api.endpoint

import arrow.core.flatMap
import io.harmny.api.instruments.ContextProvider
import io.harmny.api.model.asResponse
import io.harmny.api.model.request.ActivitiesCreateRequest
import io.harmny.api.model.request.ActivitiesListRequest
import io.harmny.api.model.request.ActivitiesUpdateRequest
import io.harmny.api.model.request.ActivityRepetitionsCreateRequest
import io.harmny.api.model.request.ActivityRepetitionsListRequest
import io.harmny.api.service.ActivitiesService
import io.harmny.api.service.ActivityRepetitionsService
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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
    private val activityRepetitionsService: ActivityRepetitionsService,
) {

    @Operation(summary = "Create activity.")
    @PostMapping(path = ["/activities"])
    suspend fun createActivity(
        @RequestHeader headers: HttpHeaders,
        @RequestBody request: ActivitiesCreateRequest,
    ): ResponseEntity<out Any> {
        return contextProvider.provide().flatMap { context ->
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
        @RequestHeader("Authorization") authorization: String,
        request: ActivitiesListRequest,
    ): ResponseEntity<out Any> {
        return contextProvider.provide().map { context ->
            withContext(Dispatchers.IO) {
                activitiesService.list(context)
            }
        }.fold(
            { fail -> fail.asResponse() },
            { activities -> ResponseEntity.ok(activities) },
        )
    }

    @Operation(summary = "Update activity.")
    @PutMapping(path = ["/activities/{activityId}"])
    suspend fun updateActivity(
        @RequestHeader headers: HttpHeaders,
        @PathVariable("activityId") activityId: String,
        @RequestBody request: ActivitiesUpdateRequest,
    ): ResponseEntity<out Any> {
        return contextProvider.provide().flatMap { context ->
            withContext(Dispatchers.IO) {
                activitiesService.update(context, activityId, request)
            }
        }.fold(
            { fail -> fail.asResponse() },
            { activity -> ResponseEntity.ok(activity) },
        )
    }

    @Operation(summary = "Delete activity.")
    @DeleteMapping(path = ["/activities/{activityId}"])
    suspend fun deleteActivity(
        @RequestHeader headers: HttpHeaders,
        @PathVariable("activityId") activityId: String,
    ): ResponseEntity<out Any> {
        return contextProvider.provide().flatMap { context ->
            withContext(Dispatchers.IO) {
                activitiesService.delete(context, activityId)
            }
        }.fold(
            { fail -> fail.asResponse() },
            { activity -> ResponseEntity.ok(activity) },
        )
    }

    @Operation(summary = "List activity repetitions.")
    @GetMapping(path = ["/activities/{activityId}/repetitions"])
    suspend fun listActivityRepetitions(
        @RequestHeader headers: HttpHeaders,
        @PathVariable("activityId") activityId: String,
        request: ActivityRepetitionsListRequest,
    ): ResponseEntity<out Any> {
        return contextProvider.provide().flatMap { context ->
            withContext(Dispatchers.IO) {
                activityRepetitionsService.list(context, activityId, request)
            }
        }.fold(
            { fail -> fail.asResponse() },
            { page -> ResponseEntity.ok(page) },
        )
    }

    @Operation(summary = "Create activity repetition.")
    @PostMapping(path = ["/activities/{activityId}/repetitions"])
    suspend fun createActivityRepetition(
        @RequestHeader headers: HttpHeaders,
        @PathVariable("activityId") activityId: String,
        @RequestBody request: ActivityRepetitionsCreateRequest,
    ): ResponseEntity<out Any> {
        return contextProvider.provide().flatMap { context ->
            withContext(Dispatchers.IO) {
                activityRepetitionsService.create(context, activityId, request)
            }
        }.fold(
            { fail -> fail.asResponse() },
            { repetition -> ResponseEntity.ok(repetition) },
        )
    }

    @Operation(summary = "Complete activity repetition.")
    @PatchMapping(path = ["/activities/{activityId}/repetitions/{repetitionId}"])
    suspend fun completeActivityRepetition(
        @RequestHeader headers: HttpHeaders,
        @PathVariable("activityId") activityId: String,
        @PathVariable("repetitionId") repetitionId: String,
    ): ResponseEntity<out Any> {
        return contextProvider.provide().flatMap { context ->
            withContext(Dispatchers.IO) {
                activityRepetitionsService.complete(context, activityId, repetitionId)
            }
        }.fold(
            { fail -> fail.asResponse() },
            { repetition -> ResponseEntity.ok(repetition) },
        )
    }

    @Operation(summary = "Delete activity repetition.")
    @DeleteMapping(path = ["/activities/{activityId}/repetitions/{repetitionId}"])
    suspend fun deleteActivityRepetition(
        @RequestHeader headers: HttpHeaders,
        @PathVariable("activityId") activityId: String,
        @PathVariable("repetitionId") repetitionId: String,
    ): ResponseEntity<out Any> {
        return contextProvider.provide().flatMap { context ->
            withContext(Dispatchers.IO) {
                activityRepetitionsService.delete(context, activityId, repetitionId)
            }
        }.fold(
            { fail -> fail.asResponse() },
            { repetition -> ResponseEntity.ok(repetition) },
        )
    }
}
