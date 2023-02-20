package io.harmny.api.entity

import java.time.Instant
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany

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
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "routine_2_routine_item",
        joinColumns = [JoinColumn(name = "routine_id")],
        inverseJoinColumns = [JoinColumn(name = "routine_item_id")],
    )
    var routineItems: List<RoutineItemEntity>,
)
