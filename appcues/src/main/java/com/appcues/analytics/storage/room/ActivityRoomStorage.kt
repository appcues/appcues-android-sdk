package com.appcues.analytics.storage.room

import android.content.Context
import androidx.room.Room
import com.appcues.analytics.storage.ActivityStorage
import com.appcues.analytics.storage.ActivityStoring

internal class ActivityRoomStorage(
    context: Context,
) : ActivityStoring {

    private val db = Room.databaseBuilder(context, ActivityDatabase::class.java, "appcues-activity").build()

    override suspend fun save(activityStorage: ActivityStorage) =
        db.activityStorageDao().insertAll(activityStorage)

    override suspend fun remove(activityStorage: ActivityStorage) =
        db.activityStorageDao().delete(activityStorage)

    override suspend fun read(): List<ActivityStorage> =
        db.activityStorageDao().getAll()
}
