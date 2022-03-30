package com.appcues.data

import com.appcues.data.remote.response.QualifyResponse
import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.data.remote.retrofit.deserializer.QualifyResponseDeserializer
import com.appcues.data.remote.retrofit.deserializer.StepContainerResponseDeserializer
import com.appcues.data.remote.retrofit.serializer.DateSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.Date

internal object GsonConfiguration {
    fun getGson(): Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, DateSerializer())
        .registerTypeAdapter(QualifyResponse::class.java, QualifyResponseDeserializer())
        .registerTypeAdapter(StepContainerResponse::class.java, StepContainerResponseDeserializer())
        .create()
}
