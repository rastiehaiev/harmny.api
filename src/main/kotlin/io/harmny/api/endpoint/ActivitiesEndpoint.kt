package io.harmny.api.endpoint

import io.harmny.api.annotation.CurrentContext
import io.harmny.api.model.Context
import io.harmny.api.model.asResponse
import io.harmny.api.model.request.ActivitiesCreateRequest
import io.harmny.api.model.request.ActivitiesListRequest
import io.harmny.api.model.request.ActivitiesUpdateRequest
import io.harmny.api.model.request.ActivityRepetitionsCreateRequest
import io.harmny.api.model.request.ActivityRepetitionsListRequest
import io.harmny.api.model.request.ActivityRepetitionsStartRequest
import io.harmny.api.model.request.ActivityRepetitionsUpdateRequest
import io.harmny.api.service.ActivitiesService
import io.harmny.api.service.ActivityRepetitionsService
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivitiesEndpoint(
    private val activitiesService: ActivitiesService,
    private val activityRepetitionsService: ActivityRepetitionsService,
) {

    @Operation(summary = "Create activity.")
    @PostMapping(path = ["/activities"])
    suspend fun createActivity(
        @CurrentContext context: Context,
        @RequestBody request: ActivitiesCreateRequest,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            activitiesService.create(context, request)
        }.fold(
            { fail -> fail.asResponse() },
            { activity -> ResponseEntity.status(HttpStatus.CREATED).body(activity) },
        )
    }

    @Operation(summary = "List activities.")
    @GetMapping(path = ["/activities"])
    suspend fun listActivities(
        @CurrentContext context: Context,
        request: ActivitiesListRequest,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            ResponseEntity.ok(activitiesService.list(context))
        }
    }

    @Operation(summary = "Update activity.")
    @PutMapping(path = ["/activities/{activityId}"])
    suspend fun updateActivity(
        @CurrentContext context: Context,
        @PathVariable("activityId") activityId: String,
        @RequestBody request: ActivitiesUpdateRequest,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            activitiesService.update(context, activityId, request)
        }.fold(
            { fail -> fail.asResponse() },
            { activity -> ResponseEntity.ok(activity) },
        )
    }

    @Operation(summary = "Delete activity.")
    @DeleteMapping(path = ["/activities/{activityId}"])
    suspend fun deleteActivity(
        @CurrentContext context: Context,
        @PathVariable("activityId") activityId: String,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            activitiesService.delete(context, activityId)
        }.fold(
            { fail -> fail.asResponse() },
            { activity -> ResponseEntity.ok(activity) },
        )
    }

    @Operation(summary = "List activity repetitions.")
    @GetMapping(path = ["/activities/{activityId}/repetitions"])
    suspend fun listActivityRepetitions(
        @CurrentContext context: Context,
        @PathVariable("activityId") activityId: String,
        request: ActivityRepetitionsListRequest,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            activityRepetitionsService.list(context, activityId, request)
        }.fold(
            { fail -> fail.asResponse() },
            { page -> ResponseEntity.ok(page) },
        )
    }

    @Operation(summary = "Get activity repetition.")
    @GetMapping(path = ["/activities/{activityId}/repetitions/{repetitionId}"])
    suspend fun getActivityRepetition(
        @CurrentContext context: Context,
        @PathVariable("activityId") activityId: String,
        @PathVariable("repetitionId") repetitionId: String,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            activityRepetitionsService.get(context, activityId, repetitionId)
        }.fold(
            { fail -> fail.asResponse() },
            { repetition -> ResponseEntity.ok(repetition) },
        )
    }

    @Operation(summary = "Create activity repetition.")
    @PostMapping(path = ["/activities/{activityId}/repetitions"])
    suspend fun createActivityRepetition(
        @CurrentContext context: Context,
        @PathVariable("activityId") activityId: String,
        @RequestBody request: ActivityRepetitionsCreateRequest,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            activityRepetitionsService.create(context, activityId, request)
        }.fold(
            { fail -> fail.asResponse() },
            { repetition -> ResponseEntity.ok(repetition) },
        )
    }

    @Operation(summary = "Start activity repetition.")
    @PostMapping(path = ["/activities/{activityId}/repetitions/{repetitionId}/start"])
    suspend fun startActivityRepetition(
        @CurrentContext context: Context,
        @PathVariable("activityId") activityId: String,
        @PathVariable("repetitionId") repetitionId: String,
        @RequestBody request: ActivityRepetitionsStartRequest?,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            activityRepetitionsService.start(context, activityId, repetitionId, request)
        }.fold(
            { fail -> fail.asResponse() },
            { repetition -> ResponseEntity.ok(repetition) },
        )
    }

    @Operation(summary = "Pause activity repetition.")
    @PostMapping(path = ["/activities/{activityId}/repetitions/{repetitionId}/pause"])
    suspend fun pauseActivityRepetition(
        @CurrentContext context: Context,
        @PathVariable("activityId") activityId: String,
        @PathVariable("repetitionId") repetitionId: String,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            activityRepetitionsService.pause(context, activityId, repetitionId)
        }.fold(
            { fail -> fail.asResponse() },
            { repetition -> ResponseEntity.ok(repetition) },
        )
    }

    @Operation(summary = "Update activity repetition.")
    @PutMapping(path = ["/activities/{activityId}/repetitions/{repetitionId}"])
    suspend fun updateActivityRepetition(
        @CurrentContext context: Context,
        @PathVariable("activityId") activityId: String,
        @PathVariable("repetitionId") repetitionId: String,
        @RequestBody request: ActivityRepetitionsUpdateRequest,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            activityRepetitionsService.update(context, activityId, repetitionId, request)
        }.fold(
            { fail -> fail.asResponse() },
            { repetition -> ResponseEntity.ok(repetition) },
        )
    }

    @Operation(summary = "Delete activity repetition.")
    @DeleteMapping(path = ["/activities/{activityId}/repetitions/{repetitionId}"])
    suspend fun deleteActivityRepetition(
        @CurrentContext context: Context,
        @PathVariable("activityId") activityId: String,
        @PathVariable("repetitionId") repetitionId: String,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            activityRepetitionsService.delete(context, activityId, repetitionId)
        }.fold(
            { fail -> fail.asResponse() },
            { repetition -> ResponseEntity.ok(repetition) },
        )
    }
}
