package com.appcues.data.local.room

import androidx.room.TypeConverter
import java.util.Date

object DateConverter {

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}
