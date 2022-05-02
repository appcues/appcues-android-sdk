package com.appcues.data.remote.adapters

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.data.remote.response.trait.TraitResponse
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class StepOrContainerResponse(
    val id: UUID,
    val children: List<StepResponse>?,
    val content: StepContentResponse?,
    val traits: List<TraitResponse>,
    val actions: Map<UUID, List<ActionResponse>>
)

internal class StepContainerAdapter {

    @FromJson
    fun fromJson(stepOrContainer: StepOrContainerResponse): StepContainerResponse {
        return when {
            stepOrContainer.content != null ->
                StepContainerResponse(
                    id = UUID.randomUUID(),
                    children = arrayListOf(StepResponse(stepOrContainer.id, stepOrContainer.content, listOf(), hashMapOf())),
                    traits = stepOrContainer.traits,
                    actions = stepOrContainer.actions,
                )
            stepOrContainer.children != null ->
                StepContainerResponse(
                    id = stepOrContainer.id,
                    children = stepOrContainer.children,
                    traits = stepOrContainer.traits,
                    actions = stepOrContainer.actions
                )
            else -> throw JsonDataException("invalid step container response, must have either content or children")
        }
    }

    @ToJson
    @Suppress("UnusedPrivateMember", "UNUSED_PARAMETER") // required by Moshi
    fun toJson(value: StepContainerResponse): String {
        throw UnsupportedOperationException("step container only supports deserialization")
    }
}
