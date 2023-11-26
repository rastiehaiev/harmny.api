package io.harmny.api.entity

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import javax.persistence.ColumnResult
import javax.persistence.ConstructorResult
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.NamedNativeQuery
import javax.persistence.SqlResultSetMapping

@Entity
@NamedNativeQuery(
    name = "find_activity_metric_line_chart",
    query = """
        SELECT DATE(ar.started_at) AS truncated_date, SUM(ar.time_spent_ms) AS time_spent_ms_total, SUM(ar.count) AS count_total
        FROM activity_repetition ar
        WHERE ar.activity_id = :activity_id
          AND ar.started_at BETWEEN :startTime AND :endTime
          AND (:application_id IS NULL OR application_id = :application_id)
          AND ar.completed IS TRUE
        GROUP BY truncated_date;
    """,
    resultSetMapping = "activity_metric_line_chart_dto",
)
@SqlResultSetMapping(
    name = "activity_metric_line_chart_dto",
    classes = [
        ConstructorResult(
            targetClass = ActivityLineChartMetricItem::class,
            columns = [
                ColumnResult(name = "truncated_date", type = String::class),
                ColumnResult(name = "time_spent_ms_total", type = Int::class),
                ColumnResult(name = "count_total", type = Int::class),
            ]
        )
    ]
)
class ActivityMetricLineChartEntity {
    @Id
    @GeneratedValue
    var id: Long = 0
}

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ActivityLineChartMetricItem(
    val truncatedDate: String,
    val timeSpentMsTotal: Int?,
    val countTotal: Int?,
)
