package com.appcues.analytics.storage.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appcues.analytics.storage.ActivityStorage

@Database(entities = [ActivityStorage::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
internal abstract class ActivityDatabase : RoomDatabase() {
    abstract fun activityStorageDao(): ActivityStorageDao
}
