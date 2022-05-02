package com.appcues.data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.Date

internal class DateAdapter {

    @ToJson
    fun toJson(date: Date): Long {
        return date.time
    }

    @FromJson
    fun fromJson(timestamp: Long): Date {
        return Date(timestamp)
    }
}
