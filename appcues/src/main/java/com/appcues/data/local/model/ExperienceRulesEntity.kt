package com.appcues.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
internal data class ExperienceRulesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val experienceId: UUID,
    val userId: String,
    val seenAt: Date,
)
