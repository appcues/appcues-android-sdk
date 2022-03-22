package com.appcues.data.local

import com.appcues.data.local.model.ActivityStorage

internal interface AppcuesLocalSource {
    suspend fun save(activityStorage: ActivityStorage)
    suspend fun remove(activityStorage: ActivityStorage)
    suspend fun read(): List<ActivityStorage>
}
