package io.harmny.api.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity(name = "routine_item")
data class RoutineItemEntity(
    @Id
    val id: String,
    @Column(name = "idx")
    var index: Int,
    @Column(name = "note")
    var note: String?,
    @ManyToOne
    val activity: ActivityEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    val routine: RoutineEntity,
)
