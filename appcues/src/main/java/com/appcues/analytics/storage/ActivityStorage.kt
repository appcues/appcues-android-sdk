package com.appcues.analytics.storage

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.appcues.data.remote.request.ActivityRequest
import com.google.gson.Gson
import java.util.Date
import java.util.UUID

@Entity
internal data class ActivityStorage(
    @PrimaryKey val requestId: UUID,
    val accountId: String,
    val userID: String,
    val data: String, // encoded JSON POST body
    val created: Date,
) {
    constructor(activityRequest: ActivityRequest, gson: Gson) :
        this(activityRequest.requestId, activityRequest.accountId, activityRequest.userId, gson.toJson(activityRequest), Date())
}
