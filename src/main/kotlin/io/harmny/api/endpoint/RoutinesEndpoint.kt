package io.harmny.api.endpoint

import io.harmny.api.annotation.CurrentContext
import io.harmny.api.model.Context
import io.harmny.api.model.asResponse
import io.harmny.api.model.request.RoutinesCreateRequest
import io.harmny.api.model.request.RoutinesListRequest
import io.harmny.api.model.request.RoutinesUpdateRequest
import io.harmny.api.service.RoutinesService
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
class RoutinesEndpoint(
    private val routinesService: RoutinesService,
) {

    @Operation(summary = "Create routine.")
    @PostMapping(path = ["/routines"])
    suspend fun createRoutine(
        @CurrentContext context: Context,
        @RequestBody request: RoutinesCreateRequest,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            routinesService.create(context, request)
        }.fold(
            { fail -> fail.asResponse() },
            { routine -> ResponseEntity.status(HttpStatus.CREATED).body(routine) },
        )
    }

    @Operation(summary = "List routines.")
    @GetMapping(path = ["/routines"])
    suspend fun listRoutines(
        @CurrentContext context: Context,
        request: RoutinesListRequest,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            routinesService.list(context, request).let { ResponseEntity.ok(it) }
        }
    }

    @Operation(summary = "Get routine details.")
    @GetMapping(path = ["/routines/{routineId}"])
    suspend fun getRoutine(
        @CurrentContext context: Context,
        @PathVariable("routineId") routineId: String,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            routinesService.getDetails(context, routineId)
        }.fold(
            { fail -> fail.asResponse() },
            { routine -> ResponseEntity.ok(routine) },
        )
    }

    @Operation(summary = "Update routine.")
    @PutMapping(path = ["/routines/{routineId}"])
    suspend fun updateActivity(
        @CurrentContext context: Context,
        @PathVariable("routineId") routineId: String,
        @RequestBody request: RoutinesUpdateRequest,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            routinesService.update(context, routineId, request)
        }.fold(
            { fail -> fail.asResponse() },
            { activity -> ResponseEntity.ok(activity) },
        )
    }

    @Operation(summary = "Delete routine.")
    @DeleteMapping(path = ["/routines/{routineId}"])
    suspend fun deleteRoutine(
        @CurrentContext context: Context,
        @PathVariable("routineId") routineId: String,
    ): ResponseEntity<out Any> {
        return withContext(Dispatchers.IO) {
            routinesService.delete(context, routineId)
        }.fold(
            { fail -> fail.asResponse() },
            { routine -> ResponseEntity.ok(routine) },
        )
    }
}
