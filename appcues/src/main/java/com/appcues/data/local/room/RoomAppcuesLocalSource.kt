package com.appcues.data.local.room

import com.appcues.data.local.AppcuesLocalSource
import com.appcues.data.local.model.ActivityStorage

internal class RoomAppcuesLocalSource(
    private val db: AppcuesDatabase
) : AppcuesLocalSource {

    override suspend fun saveActivity(activityStorage: ActivityStorage) =
        db.activityStorageDao().insertAll(activityStorage)

    override suspend fun removeActivity(activityStorage: ActivityStorage) =
        db.activityStorageDao().delete(activityStorage)

    override suspend fun getAllActivity(): List<ActivityStorage> =
        db.activityStorageDao().getAll()
}
