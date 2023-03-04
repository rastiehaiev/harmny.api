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
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.transaction.Transactional

@Service
class ActivityRepetitionsService(
    private val activitiesRepository: ActivitiesRepository,
    private val activityRepetitionsRepository: ActivityRepetitionsRepository,
) {

    @Transactional
    fun create(
        context: Context,
        activityId: String,
        request: ActivityRepetitionsCreateRequest,
    ): Either<Fail, ActivityRepetition> {
        val caloriesBurnt = request.caloriesBurnt?.let {
            it.validateNumber(
                key = "activity.repetition.calories.burnt.invalid",
                minValue = 0,
                maxValue = 100,
            ).fix { fail -> return fail.left() }
        }
        val count = request.count?.let {
            it.validateNumber(
                key = "activity.repetition.count.invalid",
                minValue = 0,
                maxValue = 1000000,
            ).fix { fail -> return fail.left() }
        }

        val timeSpentMs = request.timeSpentMs?.let {
            it.validateNumber(
                key = "activity.repetition.time.spent.ms.invalid",
                minValue = 0,
                maxValue = TimeUnit.DAYS.toMillis(5).toInt(),
            ).fix { fail -> return fail.left() }
        }

        val activity = findActivity(context, activityId).fix { return it.left() }
        val completed = request.completed?.toBooleanStrictOrNull() ?: true
        if (!completed && activity.currentRepetition != null) {
            return Fail.input("activity.repetition.already.in.progress")
        }

        val activityRepetitionEntity = ActivityRepetitionEntity(
            id = UUID.randomUUID().toString(),
            applicationId = context.applicationId,
            createdAt = Instant.now(),
            count = count,
            caloriesBurnt = caloriesBurnt,
            timeSpentMs = timeSpentMs,
            activity = activity,
            completed = completed,
        )

        activityRepetitionsRepository.save(activityRepetitionEntity)
        if (!completed) {
            activity.currentRepetition = activityRepetitionEntity
            activitiesRepository.save(activity)
        }
        return activityRepetitionEntity.toModel().right()
    }

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
    fun delete(
        context: Context,
        activityId: String,
        repetitionId: String,
    ): Either<Fail, ActivityRepetition> {
        val activityRepetition = findActivityRepetition(context, activityId, repetitionId).fix { return it.left() }
        activityRepetitionsRepository.delete(activityRepetition)
        return activityRepetition.toModel().right()
    }

    @Transactional
    fun complete(
        context: Context,
        activityId: String,
        repetitionId: String,
    ): Either<Fail, ActivityRepetition> {
        val activityRepetition = findActivityRepetition(context, activityId, repetitionId).fix { return it.left() }
        activityRepetition.completed = true
        activityRepetitionsRepository.save(activityRepetition)

        val activity = activityRepetition.activity
        activity.currentRepetition = null
        activitiesRepository.save(activity)
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
}

private fun ActivityRepetitionEntity.toModel() = ActivityRepetition(
    id,
    timeSpentMs,
    count,
    caloriesBurnt,
    createdAt,
)
