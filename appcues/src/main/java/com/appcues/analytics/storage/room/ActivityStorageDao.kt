package com.appcues.analytics.storage.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.appcues.analytics.storage.ActivityStorage

@Dao
internal interface ActivityStorageDao {
    @Query("SELECT * FROM ActivityStorage ORDER BY created desc")
    fun getAll(): List<ActivityStorage>

    @Insert
    fun insertAll(vararg activityStorage: ActivityStorage)

    @Delete
    fun delete(activityStorage: ActivityStorage)
}
