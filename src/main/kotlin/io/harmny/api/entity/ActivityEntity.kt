package io.harmny.api.entity

import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne

@Entity(name = "activity")
data class ActivityEntity(
    @Id
    val id: String,
    @Column(name = "user_id")
    val userId: String,
    @Column(name = "application_id")
    val applicationId: String?,
    @Column(name = "parent_activity_id")
    var parentActivityId: String?,
    @Column(name = "name")
    var name: String,
    @Column(name = "is_group")
    val group: Boolean,
    @Column(name = "created_at")
    val createdAt: Instant,
    @Column(name = "last_updated_at")
    var lastUpdatedAt: Instant,
    @OneToMany(mappedBy = "activity", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var repetitions: List<ActivityRepetitionEntity> = emptyList(),
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "current_repetition_id", referencedColumnName = "id")
    var currentRepetition: ActivityRepetitionEntity? = null,
)
