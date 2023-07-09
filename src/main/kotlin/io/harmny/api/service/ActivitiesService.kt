package io.harmny.api.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.rightIfNotNull
import io.harmny.api.entity.ActivityEntity
import io.harmny.api.model.Activity
import io.harmny.api.model.Context
import io.harmny.api.model.Fail
import io.harmny.api.model.Fails
import io.harmny.api.model.request.ActivitiesCreateRequest
import io.harmny.api.model.request.ActivitiesUpdateRequest
import io.harmny.api.repository.ActivitiesRepository
import io.harmny.api.utils.fix
import io.harmny.api.utils.reduceRepeatedSpaces
import io.harmny.api.utils.validateName
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import javax.transaction.Transactional

@Service
class ActivitiesService(
    private val activitiesRepository: ActivitiesRepository,
) {

    companion object {
        val activitiesComparator = compareBy<Activity>({ !it.group }, { it.name.lowercase() })
    }

    fun list(context: Context): List<Activity> {
        val activityEntities = activitiesRepository.findAllByUserId(context.userId)
        val activitiesGroupedByParent = activityEntities.groupByTo(HashMap()) { it.parentActivityId }
        val rootActivities = activitiesGroupedByParent[null] ?: emptyList()
        return rootActivities
            .map { activityEntity -> activityEntity.toActivity(activitiesGroupedByParent) }
            .sortedWith(activitiesComparator)
    }

    @Transactional
    fun create(
        context: Context,
        request: ActivitiesCreateRequest,
    ): Either<Fail, Activity> {
        val parentActivityId = request.parentActivityId?.let {
            val parent = findValidParent(context, it).fix { fail -> return fail.left() }
            val dimensions = activitiesRepository.findParentsCount(parent.id)
            it.takeIf { dimensions < 4 } ?: return Fail.input("activity.dimension.exceeded")
        }

        val name = validateName(context, request.name, parentActivityId).fix { return it.left() }

        val now = Instant.now()
        val activityEntity = ActivityEntity(
            id = UUID.randomUUID().toString(),
            userId = context.userId,
            applicationId = context.applicationId,
            parentActivityId = parentActivityId,
            name = name,
            group = request.group?.lowercase()?.toBooleanStrictOrNull() ?: false,
            createdAt = now,
            lastUpdatedAt = now,
        )
        // there are no children in a newly created activity
        return activitiesRepository.save(activityEntity).toActivityWithoutChildren().right()
    }


    @Transactional
    fun update(
        context: Context,
        activityId: String,
        request: ActivitiesUpdateRequest,
    ): Either<Fail, Activity> {
        context.applicationId?.also { return Fail.authorization("activity.removal.not.allowed") }
        val activityEntity = findById(context, activityId).fix { return it.left() }
        val activityEntityCopy = activityEntity.copy()

        val parentActivityId = (request.parentActivityId ?: activityEntity.parentActivityId)
            .takeIf { !it.equals("root", ignoreCase = true) }
            ?.let { findValidParent(context, it).fix { fail -> return fail.left() }.id }

        val parentActivityDimension = parentActivityId?.let { activitiesRepository.findParentsCount(it) } ?: 0
        val activityDownDimension = if (!activityEntity.group) 0 else activitiesRepository.findChildrenCount(activityId)
        if (parentActivityDimension + activityDownDimension >= 4) {
            return Fail.input("activity.dimension.exceeded")
        }

        // prone to raise conditions [HARMNY-T-30]
        request.name?.let {
            validateName(context, it, parentActivityId).fix { fail -> return fail.left() }
        }?.also { activityEntity.name = it }

        activityEntity.parentActivityId = parentActivityId
        if (activityEntity != activityEntityCopy) {
            activityEntity.lastUpdatedAt = Instant.now()
            activitiesRepository.save(activityEntity)
        }
        return activityEntity.toActivityWithChildren().right()
    }

    @Transactional
    fun delete(
        context: Context,
        activityId: String,
    ): Either<Fail, Activity> {
        context.applicationId?.also { return Fail.authorization("activity.removal.not.allowed") }
        return findById(context, activityId).map { activityEntity ->
            activitiesRepository.delete(activityEntity)
            activityEntity.toActivityWithChildren()
        }
    }

    private fun findValidParent(context: Context, id: String): Either<Fail, ActivityEntity> {
        return findById(context, id).flatMap { activity ->
            activity.takeIf { it.group }?.right() ?: Fail.input("activity.parent.invalid")
        }
    }

    private fun findById(context: Context, activityId: String): Either<Fail, ActivityEntity> {
        return activitiesRepository.findByIdOrNull(activityId)
            ?.takeIf { it.userId == context.userId }
            ?.takeIf { context.applicationId == null || it.applicationId == context.applicationId }
            .rightIfNotNull { Fails.resource("activity.not.found") }
    }

    private fun validateName(context: Context, name: String?, parentActivityId: String?): Either<Fail, String> {
        return (name ?: "")
            .trim()
            .reduceRepeatedSpaces()
            .validateName("activity.name", minLength = 2, maxLength = 100)
            .flatMap { validatedName ->
                if (activityByNameAlreadyExists(context, parentActivityId, validatedName)) {
                    validatedName.right()
                } else Fail.input("activity.with.such.name.exists")
            }
    }

    private fun activityByNameAlreadyExists(
        context: Context,
        parentActivityId: String?,
        validatedName: String
    ) = activitiesRepository.findFirstByUserIdAndParentActivityIdAndName(
        context.userId,
        parentActivityId,
        validatedName
    ) == null

    private fun ActivityEntity.toActivityWithoutChildren(): Activity {
        return Activity(
            id,
            name,
            group,
            parentActivityId,
            createdAt,
            lastUpdatedAt,
            childActivities = null,
            currentRepetitionId = currentRepetition?.id,
        )
    }

    private fun ActivityEntity.toActivityWithChildren(): Activity {
        val activityEntity = this
        return if (activityEntity.group) {
            val children = activitiesRepository.findByIdWithChildren(activityEntity.id)
                .groupByTo(HashMap()) { it.parentActivityId }
            activityEntity.toActivity(children)
        } else activityEntity.toActivityWithoutChildren()
    }

    private fun ActivityEntity.toActivity(
        activitiesGroupedByParent: Map<String?, MutableList<ActivityEntity>>,
    ): Activity {
        val children = if (group) getChildActivities(id, activitiesGroupedByParent) else null
        return Activity(
            id,
            name,
            group,
            parentActivityId,
            createdAt,
            lastUpdatedAt,
            children,
            currentRepetition?.id,
        )
    }

    private fun getChildActivities(
        activityId: String,
        activitiesGroupedByParent: Map<String?, MutableList<ActivityEntity>>,
    ): List<Activity>? {
        val children = activitiesGroupedByParent[activityId] ?: return null
        return children.map { it.toActivity(activitiesGroupedByParent) }.sortedWith(activitiesComparator)
    }
}
