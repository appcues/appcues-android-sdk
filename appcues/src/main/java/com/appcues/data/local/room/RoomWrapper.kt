package com.appcues.data.local.room

import android.content.Context
import androidx.room.Room
import com.appcues.AppcuesConfig

internal class RoomWrapper(
    private val context: Context,
    private val config: AppcuesConfig,
) {
    fun create(): AppcuesDatabase {
        val dbName = "appcues-${config.applicationId}.db"
        return Room.databaseBuilder(context, AppcuesDatabase::class.java, dbName).build()
    }
}
