package io.harmny.api.entity

import java.time.Instant
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity(name = "routine")
data class RoutineEntity(
    @Id
    val id: String,
    @Column(name = "user_id")
    val userId: String,
    @Column(name = "application_id")
    val applicationId: String?,
    @Column(name = "name")
    var name: String,
    @Column(name = "created_at")
    val createdAt: Instant,
    @Column(name = "last_updated_at")
    var lastUpdatedAt: Instant,
    @OneToMany(mappedBy = "routine", fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var routineItems: List<RoutineItemEntity> = emptyList(),
)
