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
    @Column(name = "time_spent_ms")
    val timeSpentMs: Int?,
    @Column(name = "count")
    val count: Int?,
    @Column(name = "calories_burnt")
    val caloriesBurnt: Int?,
    @Column(name = "heart_rate")
    val heartRate: Int?,
    @Column(name = "mood")
    val mood: Int?,
    @Column(name = "pain_level")
    val painLevel: Int?,
    @Column(name = "distance")
    val distance: Int?,
    @Column(name = "completed")
    var completed: Boolean = true,
    @ManyToOne(fetch = FetchType.LAZY)
    val activity: ActivityEntity,
)
