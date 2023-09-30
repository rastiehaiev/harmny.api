package io.harmny.api.entity

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity(name = "activity_repetition")
data class ActivityRepetitionEntity(
    @Id
    val id: String,
    @Column(name = "application_id")
    val applicationId: String?,
    @Column(name = "created_at")
    val createdAt: Instant,
    @Column(name = "started_at")
    val startedAt: Instant,
    @Column(name = "last_started_at")
    var lastStartedAt: Instant?,
    @Column(name = "started")
    var started: Boolean? = null,
    @Column(name = "time_spent_ms")
    var timeSpentMs: Int?,
    @Column(name = "count")
    var count: Int?,
    @Column(name = "calories_burnt")
    var caloriesBurnt: Int?,
    @Column(name = "heart_rate")
    var heartRate: Int?,
    @Column(name = "mood")
    var mood: Int?,
    @Column(name = "pain_level")
    var painLevel: Int?,
    @Column(name = "complexity")
    var complexity: Int?,
    @Column(name = "distance")
    var distance: Int?,
    @Column(name = "restarts")
    var restarts: Int? = null,
    @Column(name = "completed")
    var completed: Boolean = true,
    @ManyToOne(fetch = FetchType.LAZY)
    val activity: ActivityEntity,
)
