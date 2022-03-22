package com.appcues.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appcues.data.local.model.ActivityStorage

@Database(entities = [ActivityStorage::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
internal abstract class AppcuesDatabase : RoomDatabase() {
    abstract fun activityStorageDao(): ActivityStorageDao
}
