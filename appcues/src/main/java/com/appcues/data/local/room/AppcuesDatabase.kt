package com.appcues.data.local.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appcues.data.local.model.ActivityStorage
import com.appcues.data.local.model.ExperienceRulesEntity

@Database(
    entities = [ActivityStorage::class, ExperienceRulesEntity::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ]
)
@TypeConverters(DateConverter::class)
internal abstract class AppcuesDatabase : RoomDatabase() {

    abstract fun activityStorageDao(): ActivityStorageDao

    abstract fun experienceRulesDao(): ExperienceRulesDao
}
