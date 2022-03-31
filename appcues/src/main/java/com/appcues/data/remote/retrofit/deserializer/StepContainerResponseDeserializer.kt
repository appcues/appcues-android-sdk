package com.appcues.data.remote.retrofit.deserializer

import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.data.remote.response.step.StepResponse
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type
import java.util.UUID

internal class StepContainerResponseDeserializer : JsonDeserializer<StepContainerResponse> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext?): StepContainerResponse {
        val jsonObject = json.asJsonObject
        val gson = Gson()

        return when {
            // if it does NOT have null content, its just a step
            // check for actual null (no key in data) and JsonNull (has key but null value)
            (jsonObject.get("content")?.isJsonNull ?: true).not() -> deserializeFakeStepContainer(jsonObject, gson)
            // else, its a real step container
            else -> deserializeRealStepContainer(jsonObject, gson)
        }
    }

    private fun deserializeFakeStepContainer(jsonObject: JsonObject, gson: Gson): StepContainerResponse {
        val step = gson.fromJson(jsonObject, StepResponse::class.java)
        val traits = step.traits
        val actions = step.actions
        val children = arrayListOf<StepResponse>().apply {
            // remove all traits and actions from child step because
            // we will set it in the step container
            add(step.copy(traits = arrayListOf(), actions = hashMapOf()))
        }

        return StepContainerResponse(
            id = UUID.randomUUID(),
            children = children,
            traits = traits,
            actions = actions,
        )
    }

    private fun deserializeRealStepContainer(jsonObject: JsonObject, gson: Gson): StepContainerResponse {
        return gson.fromJson(jsonObject, StepContainerResponse::class.java)
    }
}
