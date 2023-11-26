package io.harmny.api.entity

import io.harmny.api.model.ActivityStatistics
import javax.persistence.ColumnResult
import javax.persistence.ConstructorResult
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.NamedNativeQuery
import javax.persistence.SqlResultSetMapping

@Entity
@NamedNativeQuery(
    name = "find_activity_statistics",
    query = """
        WITH RECURSIVE aa AS (
            SELECT * FROM activity WHERE id=:id
            UNION ALL
            SELECT a.*
            FROM aa, activity a
            WHERE aa.id = a.parent_activity_id
        )
        SELECT COUNT(*) AS launches_count, 
            SUM(time_spent_ms) AS time_spent_ms_total, 
            SUM(count) AS repetitions_total 
        FROM aa
            INNER JOIN activity_repetition ar ON aa.id = ar.activity_id
        WHERE aa.is_group IS FALSE
            AND ar.started_at BETWEEN :startTime AND :endTime
            AND ar.completed IS TRUE
    """,
    resultSetMapping = "activity_statistics_dto",
)
@SqlResultSetMapping(
    name = "activity_statistics_dto",
    classes = [
        ConstructorResult(
            targetClass = ActivityStatistics::class,
            columns = [
                ColumnResult(name = "launches_count", type = Int::class),
                ColumnResult(name = "time_spent_ms_total", type = Long::class),
                ColumnResult(name = "repetitions_total", type = Int::class),
            ]
        )
    ]
)
class ActivityStatisticsEntity {
    @Id
    @GeneratedValue
    var id: Long = 0
}
