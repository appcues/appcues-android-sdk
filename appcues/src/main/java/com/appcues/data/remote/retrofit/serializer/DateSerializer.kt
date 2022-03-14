package com.appcues.data.remote.retrofit.serializer

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.util.Date

internal class DateSerializer : JsonSerializer<Date> {

    override fun serialize(src: Date, typeOfSrc: Type, context: JsonSerializationContext?): JsonElement {
        // serialize Date to its UnixTimestamp value
        return JsonPrimitive(src.time)
    }
}
