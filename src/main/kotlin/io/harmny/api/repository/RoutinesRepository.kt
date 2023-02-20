package io.harmny.api.repository

import io.harmny.api.entity.RoutineEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RoutinesRepository : CrudRepository<RoutineEntity, String> {

    fun findAllByUserId(userId: String): List<RoutineEntity>

    fun findByIdAndUserId(id: String, userId: String): RoutineEntity?
}
