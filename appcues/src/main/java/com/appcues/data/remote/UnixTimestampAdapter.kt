package com.appcues.data.remote

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.Date

class UnixTimestampAdapter : TypeAdapter<Date?>() {

    override fun write(out: JsonWriter, value: Date?) {
        if (value == null) {
            out.nullValue()
            return
        }
        out.value(value.time)
    }

    override fun read(`in`: JsonReader): Date? = Date(`in`.nextLong())
}
