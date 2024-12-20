package io.harmny.api.repository

import io.harmny.api.entity.ActivityEntity
import io.harmny.api.entity.ActivityLineChartMetricItem
import io.harmny.api.model.ActivityStatistics
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ActivitiesRepository : CrudRepository<ActivityEntity, String> {

    fun findAllByUserIdAndIdIn(userId: String, ids: List<String>): List<ActivityEntity>

    fun findFirstByUserIdAndParentActivityIdAndName(
        userId: String,
        parentActivityId: String?,
        name: String,
    ): ActivityEntity?

    fun findAllByUserId(userId: String): List<ActivityEntity>

    @Query(
        value = """
        WITH RECURSIVE r AS (
            SELECT * FROM activity WHERE id=:id
            UNION ALL
            SELECT a.*
            FROM r, activity a
            WHERE r.parent_activity_id IS NOT NULL AND a.id = r.parent_activity_id
        )
        SELECT COUNT(*) AS dimensions FROM r;
    """, nativeQuery = true
    )
    fun findParentsCount(@Param("id") id: String): Long

    @Query(
        value = """
        WITH RECURSIVE r AS (
            SELECT * FROM activity WHERE id=:id
            UNION ALL
            SELECT a.*
            FROM r, activity a
            WHERE r.id = a.parent_activity_id
        )
        SELECT COUNT(DISTINCT coalesce(parent_activity_id, 'a')) dimensions FROM r;
    """, nativeQuery = true
    )
    fun findChildrenCount(@Param("id") id: String): Long

    @Query(
        value = """
        WITH RECURSIVE r AS (
            SELECT * FROM activity WHERE id=:id
            UNION ALL
            SELECT a.*
            FROM r, activity a
            WHERE r.id = a.parent_activity_id
        )
        SELECT * FROM r;
    """, nativeQuery = true
    )
    fun findByIdWithChildren(@Param("id") id: String): List<ActivityEntity>

    @Query(name = "find_activity_statistics", nativeQuery = true)
    fun findStatistics(
        @Param("id") id: String,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
    ): ActivityStatistics

    @Query(name = "find_activity_metric_line_chart", nativeQuery = true)
    fun findMetricLineChart(
        @Param("activity_id") id: String,
        @Param("application_id") applicationId: String?,
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime,
    ): List<ActivityLineChartMetricItem>
}
