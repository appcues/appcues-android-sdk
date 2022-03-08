package com.appcues.data.remote.response

import com.appcues.data.remote.response.experience.ExperienceResponse
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

internal class ActivityDeserializer : JsonDeserializer<ActivityResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext?): ActivityResponse {
        val jsonObject = json.asJsonObject

        // for a non-synchronous activity request (no sync=1 param) the response simply has
        // "ok": true
        // and will not contain any experiences
        return if (jsonObject.has("ok")) {
            ActivityResponse(listOf(), true)
        } else {
            // for synchronous activity (sync=1 used on request) the response will have an array
            // of qualified experiences
            val gson = Gson()
            val experienceJson = jsonObject.getAsJsonArray("experiences")
            val experiences = mutableListOf<ExperienceResponse>()
            for (experienceResponse in experienceJson) {
                val experience = gson.fromJson(experienceResponse, ExperienceResponse::class.java)
                experiences.add(experience)
            }
            val performedQualification = jsonObject.get("performed_qualification").asBoolean
            ActivityResponse(experiences, performedQualification)
        }
    }
}
