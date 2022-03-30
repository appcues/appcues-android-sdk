package com.appcues.data.remote.retrofit.deserializer

import com.appcues.data.remote.response.QualifyResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

internal class QualifyResponseDeserializer : JsonDeserializer<QualifyResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext?): QualifyResponse {
        val jsonObject = json.asJsonObject

        val gson = Gson()
        val experienceJson = jsonObject.getAsJsonArray("experiences")
        val experiences = mutableListOf<ExperienceResponse>()
        for (experienceResponse in experienceJson) {
            val experience = gson.fromJson(experienceResponse, ExperienceResponse::class.java)
            experiences.add(experience)
        }
        val performedQualification = jsonObject.get("performed_qualification").asBoolean

        return QualifyResponse(experiences, performedQualification)
    }
}
