package com.appcues.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
internal data class ActivityStorage(
    @PrimaryKey val requestId: UUID,
    val accountId: String,
    val userId: String,
    val data: String,
    val userSignature: String?,
    val created: Date = Date(),
)
