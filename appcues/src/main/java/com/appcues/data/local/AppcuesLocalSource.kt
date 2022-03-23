package com.appcues.data.local

import com.appcues.data.local.model.ActivityStorage

internal interface AppcuesLocalSource {
    suspend fun saveActivity(activityStorage: ActivityStorage)
    suspend fun removeActivity(activityStorage: ActivityStorage)
    suspend fun getAllActivity(): List<ActivityStorage>
}
