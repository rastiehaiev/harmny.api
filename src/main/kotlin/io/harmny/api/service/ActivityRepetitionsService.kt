package io.harmny.api.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.rightIfNotNull
import io.harmny.api.entity.ActivityEntity
import io.harmny.api.entity.ActivityRepetitionEntity
import io.harmny.api.model.ActivityRepetition
import io.harmny.api.model.Context
import io.harmny.api.model.Fail
import io.harmny.api.model.Fails
import io.harmny.api.model.Page
import io.harmny.api.model.request.ActivityRepetitionsCreateRequest
import io.harmny.api.model.request.ActivityRepetitionsListRequest
import io.harmny.api.model.request.ActivityRepetitionsStartRequest
import io.harmny.api.model.request.ActivityRepetitionsUpdateRequest
import io.harmny.api.repository.ActivitiesRepository
import io.harmny.api.repository.ActivityRepetitionsRepository
import io.harmny.api.utils.fix
import io.harmny.api.utils.parsePageNumber
import io.harmny.api.utils.parsePageSize
import io.harmny.api.utils.validateNumber
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.transaction.Transactional

@Service
class ActivityRepetitionsService(
    private val activitiesRepository: ActivitiesRepository,
    private val activityRepetitionsRepository: ActivityRepetitionsRepository,
) {

    fun list(
        context: Context,
        activityId: String,
        request: ActivityRepetitionsListRequest,
    ): Either<Fail, Page<ActivityRepetition>> {
        findActivity(context, activityId).fix { return it.left() }
        val pageNumber = request.pageNumber.parsePageNumber().fix { return it.left() }
        val pageSize = request.pageSize.parsePageSize(defaultPageSize = 20, maxPageSize = 50).fix { return it.left() }
        val count = activityRepetitionsRepository.countAllByContextAndReadyTrue(context.applicationId, activityId)
        if (count == 0L) {
            return Page.empty<ActivityRepetition>(pageNumber, pageSize).right()
        }

        val repetitions = run {
            val pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "created_at"))
            activityRepetitionsRepository.findAllByContextAndReadyTrue(
                context.applicationId,
                activityId,
                pageRequest,
            ).map { it.toModel() }
        }
        return Page(total = count, pageNumber, pageSize, repetitions).right()
    }

    @Transactional
    fun get(
        context: Context,
        activityId: String,
        repetitionId: String,
    ): Either<Fail, ActivityRepetition> {
        return findActivityRepetition(context, activityId, repetitionId).map { it.toModel() }
    }

    @Transactional
    fun create(
        context: Context,
        activityId: String,
        request: ActivityRepetitionsCreateRequest,
    ): Either<Fail, ActivityRepetition> {
        val timeSpentMs = request.timeSpentMs?.let {
            it.validateNumber(
                key = "activity.repetition.time.spent.ms.invalid",
                minValue = 0,
                maxValue = TimeUnit.DAYS.toMillis(5).toInt(),
            ).fix { fail -> return fail.left() }
        }

        val now = Instant.now()
        val startedAt = request.startedAt?.toLongOrNull()?.let { millis ->
            Instant.ofEpochMilli(millis).let { instant ->
                if (instant.isAfter(now.plus(1, ChronoUnit.DAYS))) {
                    return Fail.input("activity.repetition.started.at.in.future")
                }
                if (instant.isBefore(now.minus(360, ChronoUnit.DAYS))) {
                    return Fail.input("activity.repetition.started.at.too.old")
                }
                instant.takeIf { !it.isAfter(now) }
            }
        }

        val started = request.started?.toBooleanStrictOrNull() ?: false
        val completed = !started

        val activity = findActivity(context, activityId).fix { return it.left() }
        if (!completed && activity.currentRepetition != null) {
            return Fail.input("activity.repetition.already.in.progress")
        }

        val caloriesBurnt = request.caloriesBurnt?.toCaloriesBurnt()?.fix { fail -> return fail.left() }
        val count = request.count?.toCount()?.fix { fail -> return fail.left() }
        val heartRate = request.heartRate?.toHeartRate()?.fix { fail -> return fail.left() }
        val distance = request.distance?.toDistance()?.fix { fail -> return fail.left() }
        val painLevel = request.painLevel?.toPainLevel()?.fix { fail -> return fail.left() }
        val mood = request.mood?.toMood()?.fix { fail -> return fail.left() }
        val complexity = request.complexity?.toComplexity()?.fix { fail -> return fail.left() }

        val activityRepetitionEntity = ActivityRepetitionEntity(
            id = UUID.randomUUID().toString(),
            applicationId = context.applicationId,
            createdAt = now,
            startedAt = startedAt ?: now,
            lastStartedAt = if (started) now else null,
            count = count,
            caloriesBurnt = caloriesBurnt,
            timeSpentMs = timeSpentMs,
            distance = distance,
            heartRate = heartRate,
            mood = mood,
            painLevel = painLevel,
            activity = activity,
            complexity = complexity,
            started = started.takeIf { it },
            completed = completed,
        )

        activityRepetitionsRepository.save(activityRepetitionEntity)
        if (!completed) {
            activity.currentRepetition = activityRepetitionEntity
            activitiesRepository.save(activity)
        }
        return activityRepetitionEntity.toModel().right()
    }

    @Transactional
    fun start(
        context: Context,
        activityId: String,
        repetitionId: String,
        request: ActivityRepetitionsStartRequest?,
    ): Either<Fail, ActivityRepetition> {
        val activityRepetition = findActivityRepetition(context, activityId, repetitionId).fix { return it.left() }
        val restarts = activityRepetition.restarts ?: 0
        return if (activityRepetition.started == true) {
            Fail.input("activity.repetition.already.started")
        } else if (activityRepetition.completed) {
            Fail.input("activity.repetition.already.completed")
        } else if (restarts >= 10) {
            Fail.input("activity.repetition.too.many.restarts")
        } else {
            if (request != null) {
                // set only when all fields are valid
                val caloriesBurnt = request.caloriesBurnt?.toCaloriesBurnt()?.fix { fail -> return fail.left() }
                val distance = request.distance?.toDistance()?.fix { fail -> return fail.left() }
                val count = request.count?.toCount()?.fix { fail -> return fail.left() }
                val heartRate = request.heartRate?.toHeartRate()?.fix { fail -> return fail.left() }
                val complexity = request.complexity?.toComplexity()?.fix { fail -> return fail.left() }
                val painLevel = request.painLevel?.toPainLevel()?.fix { fail -> return fail.left() }
                val mood = request.mood?.toMood()?.fix { fail -> return fail.left() }

                activityRepetition.count = count
                activityRepetition.caloriesBurnt = caloriesBurnt
                activityRepetition.distance = distance
                activityRepetition.mood = mood
                activityRepetition.heartRate = heartRate
                activityRepetition.painLevel = painLevel
                activityRepetition.complexity = complexity
            }

            activityRepetition.started = true
            activityRepetition.lastStartedAt = Instant.now()
            activityRepetition.toModel().right()
        }
    }

    @Transactional
    fun pause(
        context: Context,
        activityId: String,
        repetitionId: String,
    ): Either<Fail, ActivityRepetition> {
        val activityRepetition = findActivityRepetition(context, activityId, repetitionId).fix { return it.left() }

        val now = Instant.now()
        val lastStartedAt = activityRepetition.lastStartedAt
        return if (activityRepetition.completed) {
            Fail.input("activity.repetition.already.completed")
        } else if (activityRepetition.started != true || lastStartedAt == null || !lastStartedAt.isBefore(now)) {
            Fail.input("activity.repetition.not.started")
        } else {
            val newDuration = now.toEpochMilli() - lastStartedAt.toEpochMilli()
            activityRepetition.timeSpentMs = activityRepetition.calculateDuration(newDuration)
            activityRepetition.started = false
            activityRepetition.restarts = (activityRepetition.restarts ?: 0) + 1
            activityRepetition.toModel().right()
        }
    }

    @Transactional
    fun update(
        context: Context,
        activityId: String,
        repetitionId: String,
        request: ActivityRepetitionsUpdateRequest,
    ): Either<Fail, ActivityRepetition> {
        val activityRepetition = findActivityRepetition(context, activityId, repetitionId).fix { return it.left() }

        val now = Instant.now()
        val completed = request.completed?.toBooleanStrictOrNull() ?: false
        if (completed && activityRepetition.completed) {
            return Fail.input("activity.repetition.already.completed")
        }

        val count = request.count?.toCount()?.fix { fail -> return fail.left() }
        val calories = request.caloriesBurnt?.toCaloriesBurnt()?.fix { fail -> return fail.left() }
        val distance = request.distance?.toDistance()?.fix { fail -> return fail.left() }
        val mood = request.mood?.toMood()?.fix { fail -> return fail.left() }
        val heartRate = request.heartRate?.toHeartRate()?.fix { fail -> return fail.left() }
        val painLevel = request.painLevel?.toPainLevel()?.fix { fail -> return fail.left() }
        val complexity = request.complexity?.toComplexity()?.fix { fail -> return fail.left() }

        // set only when all fields are valid
        activityRepetition.count = count
        activityRepetition.caloriesBurnt = calories
        activityRepetition.distance = distance
        activityRepetition.mood = mood
        activityRepetition.heartRate = heartRate
        activityRepetition.painLevel = painLevel
        activityRepetition.complexity = complexity

        val lastStartedAt = activityRepetition.lastStartedAt
        if (completed) {
            val additionalDuration =
                if (activityRepetition.started == true && lastStartedAt != null && lastStartedAt.isBefore(now)) {
                    now.toEpochMilli() - lastStartedAt.toEpochMilli()
                } else 0

            activityRepetition.timeSpentMs = activityRepetition.calculateDuration(additionalDuration)
            activityRepetition.restarts = null
            activityRepetition.started = null
            activityRepetition.lastStartedAt = null
            activityRepetition.completed = true

            val activity = activityRepetition.activity
            activity.currentRepetition = null
            activitiesRepository.save(activity)
        }

        activityRepetitionsRepository.save(activityRepetition)
        return activityRepetition.toModel().right()
    }

    @Transactional
    fun delete(
        context: Context,
        activityId: String,
        repetitionId: String,
    ): Either<Fail, ActivityRepetition> {
        val activityRepetition = findActivityRepetition(context, activityId, repetitionId).fix { return it.left() }
        activityRepetitionsRepository.delete(activityRepetition)
        return activityRepetition.toModel().right()
    }

    private fun findActivityRepetition(
        context: Context,
        activityId: String,
        repetitionId: String,
    ) = activityRepetitionsRepository.findByIdOrNull(repetitionId)
        ?.takeIf { it.activity.id == activityId }
        ?.takeIf { it.activity.userId == context.userId }
        ?.takeIf { context.applicationId == null || it.activity.applicationId == context.applicationId }
        .rightIfNotNull { Fails.resource("activity.repetition.not.found") }

    private fun findActivity(context: Context, activityId: String): Either<Fail, ActivityEntity> {
        return activitiesRepository.findByIdOrNull(activityId)
            ?.takeIf { it.userId == context.userId }
            ?.takeIf { context.applicationId == null || it.applicationId == context.applicationId }
            .rightIfNotNull { Fails.resource("activity.not.found") }
            .flatMap { activityEntity ->
                activityEntity.takeIf { !it.group }
                    .rightIfNotNull { Fails.input("activity.group.not.allowed") }
            }
    }

    private fun String.toMood(): Either<Fail, Int> {
        return this.validateNumber(
            key = "activity.repetition.mood.invalid",
            minValue = 1,
            maxValue = 5,
        )
    }

    private fun String.toCaloriesBurnt(): Either<Fail, Int> {
        return this.validateNumber(
            key = "activity.repetition.calories.burnt.invalid",
            minValue = 0,
            maxValue = 10000,
        )
    }

    private fun String.toCount(): Either<Fail, Int> {
        return this.validateNumber(
            key = "activity.repetition.count.invalid",
            minValue = 0,
            maxValue = 1_000_000_000,
        )
    }

    private fun String.toDistance(): Either<Fail, Int> {
        return this.validateNumber(
            key = "activity.repetition.distance.invalid",
            minValue = 0,
            maxValue = 1_000_000,
        )
    }

    private fun String.toPainLevel(): Either<Fail, Int> {
        return this.validateNumber(
            key = "activity.repetition.pain.level.invalid",
            minValue = 1,
            maxValue = 5,
        )
    }

    private fun String.toComplexity(): Either<Fail, Int> {
        return this.validateNumber(
            key = "activity.repetition.complexity.invalid",
            minValue = 1,
            maxValue = 5,
        )
    }

    private fun String.toHeartRate(): Either<Fail, Int> {
        return this.validateNumber(
            key = "activity.repetition.heart.rate.invalid",
            minValue = 10,
            maxValue = 300,
        )
    }

    private fun ActivityRepetitionEntity.calculateDuration(durationMs: Long): Int? {
        val timeSpentMs = this.timeSpentMs ?: 0
        val newTimeSpentMs = timeSpentMs + durationMs.toInt()
        return newTimeSpentMs.takeIf { it > 0 }
    }

    private fun ActivityRepetitionEntity.toModel(): ActivityRepetition {
        return ActivityRepetition(
            id = id,
            createdAt = createdAt,
            startedAt = startedAt,
            lastStartedAt = lastStartedAt,
            count = count,
            caloriesBurnt = caloriesBurnt,
            started = started,
            timeSpentMs = timeSpentMs,
            heartRate = heartRate,
            mood = mood,
            painLevel = painLevel,
            complexity = complexity,
            distance = distance,
            restarts = restarts,
        )
    }
}
