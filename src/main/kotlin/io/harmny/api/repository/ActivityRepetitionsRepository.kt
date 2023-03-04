package io.harmny.api.repository

import io.harmny.api.entity.ActivityRepetitionEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ActivityRepetitionsRepository : JpaRepository<ActivityRepetitionEntity, String> {

    @Query(
        value = """
            SELECT * 
                FROM activity_repetition
            WHERE activity_id=:activity_id 
                AND (:application_id IS NULL OR application_id = :application_id)
        """, nativeQuery = true
    )
    fun findAllByContextAndReadyTrue(
        @Param("application_id") applicationId: String?,
        @Param("activity_id") activityId: String,
        pageable: Pageable,
    ): List<ActivityRepetitionEntity>

    @Query(
        value = """
            SELECT COUNT(*) 
                FROM activity_repetition
            WHERE activity_id=:activity_id 
                AND (:application_id IS NULL OR application_id = :application_id)
        """, nativeQuery = true
    )
    fun countAllByContextAndReadyTrue(
        @Param("application_id") applicationId: String?,
        @Param("activity_id") activityId: String,
    ): Long
}
