package io.harmny.api.repository

import io.harmny.api.entity.RoutineItemEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RoutineItemsRepository : CrudRepository<RoutineItemEntity, String>
