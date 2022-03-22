package com.appcues.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.appcues.data.local.model.ActivityStorage

@Dao
internal interface ActivityStorageDao {
    @Query("SELECT * FROM ActivityStorage ORDER BY created")
    fun getAll(): List<ActivityStorage>

    @Insert
    fun insertAll(vararg activityStorage: ActivityStorage)

    @Delete
    fun delete(activityStorage: ActivityStorage)
}
