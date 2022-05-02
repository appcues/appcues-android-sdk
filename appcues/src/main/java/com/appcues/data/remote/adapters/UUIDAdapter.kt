package com.appcues.data.remote.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.UUID

internal class UUIDAdapter {
    @FromJson
    fun fromJson(json: String): UUID {
        return UUID.fromString(json)
    }

    @ToJson
    fun toJson(value: UUID): String {
        return value.toString()
    }
}
