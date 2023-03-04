package io.harmny.api.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.core.rightIfNotNull
import io.harmny.api.entity.ActivityEntity
import io.harmny.api.entity.RoutineEntity
import io.harmny.api.entity.RoutineItemEntity
import io.harmny.api.model.Context
import io.harmny.api.model.Fail
import io.harmny.api.model.Fails
import io.harmny.api.model.Routine
import io.harmny.api.model.RoutineDetails
import io.harmny.api.model.RoutineItem
import io.harmny.api.model.request.RoutinesCreateRequest
import io.harmny.api.model.request.RoutinesListRequest
import io.harmny.api.model.request.RoutinesUpdateRequest
import io.harmny.api.repository.ActivitiesRepository
import io.harmny.api.repository.RoutineItemsRepository
import io.harmny.api.repository.RoutinesRepository
import io.harmny.api.utils.fix
import io.harmny.api.utils.reduceRepeatedSpaces
import io.harmny.api.utils.validateName
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import javax.transaction.Transactional

@Service
class RoutinesService(
    private val routinesRepository: RoutinesRepository,
    private val routinesItemsRepository: RoutineItemsRepository,
    private val activityRepository: ActivitiesRepository,
) {

    fun list(context: Context, request: RoutinesListRequest): List<Routine> {
        return routinesRepository.findAllByUserId(context.userId).let { entities ->
            if (context.applicationId != null) {
                entities.filter { it.applicationId == context.applicationId }
            } else entities
        }.map { it.toRoutine() }
    }

    @Transactional
    fun getDetails(context: Context, id: String): Either<Fail, RoutineDetails> {
        return findById(context, id).map { it.toRoutineDetails() }
    }

    @Transactional
    fun create(
        context: Context,
        request: RoutinesCreateRequest,
    ): Either<Fail, RoutineDetails> {
        val name = validateName(request.name).fix { return it.left() }

        val requestedActivityIds = request.items.orEmpty().mapNotNull { it.activityId }
        val requestedActivityIdsUnique = requestedActivityIds.distinct()
        if (requestedActivityIds.size != requestedActivityIdsUnique.size) {
            return Fail.input("routine.item.activity.ids.duplicated")
        }

        val now = Instant.now()
        val routineEntity = RoutineEntity(
            id = UUID.randomUUID().toString(),
            userId = context.userId,
            applicationId = context.applicationId,
            name = name,
            createdAt = now,
            lastUpdatedAt = now,
        )

        var index = 0
        val activityEntitiesMap = findActivitiesForRoutine(context, requestedActivityIds).fix { return it.left() }
        val routineItems = request.items.orEmpty().mapNotNull { item -> item.activityId?.let { it to item.note } }
            .mapNotNull { (activityId, rawNote) ->
                val note = rawNote?.let { validateNote(it).fix { fail -> return fail.left() } }
                val activityEntity = activityEntitiesMap[activityId]
                if (activityEntity != null) {
                    RoutineItemEntity(
                        id = UUID.randomUUID().toString(),
                        index = index++,
                        note = note,
                        activity = activityEntity,
                        routine = routineEntity,
                    )
                } else {
                    null
                }
            }

        routineEntity.routineItems = routineItems
        return routinesRepository.save(routineEntity).toRoutineDetails().right()
    }

    @Transactional
    fun update(context: Context, routineId: String, request: RoutinesUpdateRequest): Either<Fail, RoutineDetails> {
        return findById(context, routineId).map { routineEntity ->
            val newName = request.name?.also { name ->
                validateName(name).fix { return it.left() }
            } ?: routineEntity.name

            val requestedItemIds = request.items?.mapNotNull { it.id } ?: emptyList()
            val requestedItemIdsUnique = requestedItemIds.distinct()
            if (requestedItemIds.size != requestedItemIdsUnique.size) {
                return Fail.input("routine.item.ids.duplicated")
            }

            val requestedActivityIds = request.items?.mapNotNull { it.activityId } ?: emptyList()
            val requestedActivityIdsUnique = requestedActivityIds.distinct()
            if (requestedActivityIds.size != requestedActivityIdsUnique.size) {
                return Fail.input("routine.item.activity.ids.duplicated")
            }

            val currentRoutineItems = routineEntity.routineItems
            val currentRoutineItemIds = currentRoutineItems.map { it.id }
            val routineItemIdsDelta = requestedItemIds - currentRoutineItemIds.toSet()
            if (routineItemIdsDelta.isNotEmpty()) {
                return Fail.input(
                    key = "routine.item.id.unknown",
                    properties = mapOf(
                        "routine_item_id" to routineItemIdsDelta[0],
                    ),
                )
            }

            val uniquenessKeyBefore = generateRoutineUniqueness(routineEntity.name, routineEntity.routineItems)

            var index = 0
            val requestedActivities = findActivitiesForRoutine(context, requestedActivityIds).fix { return it.left() }
            val currentRoutineItemsPerActivityId = currentRoutineItems.associateBy { it.activity.id }
            val newRoutineItems = request.items.orEmpty().mapNotNull { requestedItem ->
                val requestedNote = requestedItem.note?.let { validateNote(it) }?.fix { return it.left() }
                val activityId = requestedItem.activityId
                val itemId = requestedItem.id
                if (activityId == null) {
                    null
                } else if (itemId == null) {
                    val routineItemEntity = currentRoutineItemsPerActivityId[activityId]
                    if (routineItemEntity != null) {
                        routineItemEntity.note = requestedNote
                        routineItemEntity.index = index++
                        routineItemEntity
                    } else {
                        val activityEntity = requestedActivities[activityId]
                        if (activityEntity == null) {
                            null
                        } else {
                            RoutineItemEntity(
                                id = UUID.randomUUID().toString(),
                                index = index++,
                                note = requestedNote,
                                activity = activityEntity,
                                routine = routineEntity,
                            )
                        }
                    }
                } else {
                    val activityEntity = requestedActivities[activityId]
                    if (activityEntity == null) {
                        null
                    } else {
                        val routineItemEntity = currentRoutineItemsPerActivityId[activityId]?.takeIf { it.id == itemId }
                        if (routineItemEntity == null) {
                            RoutineItemEntity(
                                id = UUID.randomUUID().toString(),
                                index = index++,
                                note = requestedNote,
                                activity = activityEntity,
                                routine = routineEntity,
                            )
                        } else {
                            routineItemEntity.note = requestedNote
                            routineItemEntity.index = index++
                            routineItemEntity
                        }
                    }
                }
            }

            val uniquenessKeyAfter = generateRoutineUniqueness(newName, newRoutineItems)
            if (uniquenessKeyBefore != uniquenessKeyAfter) {
                routineEntity.name = newName
                routineEntity.routineItems = newRoutineItems
                routineEntity.lastUpdatedAt = Instant.now()
                routinesItemsRepository.saveAll(newRoutineItems)
                routinesRepository.save(routineEntity)
            }
            routineEntity.toRoutineDetails()
        }
    }

    @Transactional
    fun delete(
        context: Context,
        routineId: String,
    ): Either<Fail, RoutineDetails> {
        return findById(context, routineId).map { routineEntity ->
            routinesRepository.delete(routineEntity)
            routineEntity.toRoutineDetails()
        }
    }

    private fun validateNote(note: String): Either<Fail, String?> {
        val trimNote = note.trim()
        return if (trimNote.isBlank()) {
            null.right()
        } else if (trimNote.length > 100) {
            Fail.input(
                key = "routine.item.note.length.exceeded",
                properties = mapOf(
                    "max_size" to 100
                ),
            )
        } else {
            trimNote.right()
        }
    }

    private fun findById(context: Context, routineId: String): Either<Fail, RoutineEntity> {
        return routinesRepository.findByIdAndUserId(routineId, context.userId)?.takeIf {
            context.applicationId == null || it.applicationId == context.applicationId
        }.rightIfNotNull { Fails.resource("routine.not.found") }
    }

    private fun validateName(name: String?): Either<Fail, String> {
        return (name ?: "")
            .trim()
            .reduceRepeatedSpaces()
            .validateName("routine.name", maxLength = 100)
    }

    private fun findActivitiesForRoutine(
        context: Context,
        activityIds: List<String>,
    ): Either<Fail, Map<String, ActivityEntity>> {
        val uniqueActivityIds = activityIds.distinct()
        val activitiesMap = activityRepository.findAllByUserIdAndIdIn(context.userId, uniqueActivityIds)
            .filter { context.applicationId == null || it.applicationId == context.applicationId }
            .filter {
                if (it.group) {
                    return Fail.input("activity.group.not.allowed")
                } else true
            }.associateBy { it.id }

        val notFoundActivities = ArrayList(uniqueActivityIds).also { it.removeAll(activitiesMap.keys) }
        if (notFoundActivities.isNotEmpty()) {
            return Fail.resource(
                key = "activity.not.found",
                properties = mapOf(
                    "activity_id" to notFoundActivities[0],
                ),
            )
        }
        return activitiesMap.right()
    }
}

private data class RoutineUniqueness(
    val name: String,
    val items: List<RoutineUniquenessItem>?,
)

private data class RoutineUniquenessItem(
    val id: String,
    val activityId: String,
    val note: String?,
)

private fun generateRoutineUniqueness(name: String, routineItems: List<RoutineItemEntity>) = RoutineUniqueness(
    name,
    routineItems.map { RoutineUniquenessItem(it.id, it.activity.id, it.note) },
)

private fun RoutineEntity.toRoutine() = Routine(id, name, createdAt, lastUpdatedAt)

private fun RoutineItemEntity.toRoutineItem() = RoutineItem(id, activity.id, activity.name, note)

private fun RoutineEntity.toRoutineDetails() = RoutineDetails(
    id,
    name,
    createdAt,
    lastUpdatedAt,
    items = routineItems.sortedBy { it.index }.map { it.toRoutineItem() },
)
