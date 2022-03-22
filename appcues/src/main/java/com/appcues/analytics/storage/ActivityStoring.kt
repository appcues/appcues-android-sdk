package com.appcues.analytics.storage

internal interface ActivityStoring {
    suspend fun save(activityStorage: ActivityStorage)
    suspend fun remove(activityStorage: ActivityStorage)
    suspend fun read(): List<ActivityStorage>
}
