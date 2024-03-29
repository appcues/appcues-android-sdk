package com.appcues.data.remote.appcues.adapters

import com.appcues.data.remote.appcues.response.action.ActionResponse
import com.appcues.data.remote.appcues.response.step.StepContainerResponse
import com.appcues.data.remote.appcues.response.step.StepResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.appcues.response.trait.TraitResponse
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class StepOrContainerResponse(
    val id: UUID,
    val children: List<StepResponse>?,
    val content: PrimitiveResponse?,
    val traits: List<TraitResponse>,
    val actions: Map<UUID, List<ActionResponse>>,
    val type: String,
)

internal class StepContainerAdapter {

    @FromJson
    fun fromJson(stepOrContainer: StepOrContainerResponse): StepContainerResponse {
        return when {
            stepOrContainer.content != null ->
                StepContainerResponse(
                    id = UUID.randomUUID(),
                    children = arrayListOf(
                        StepResponse(
                            id = stepOrContainer.id,
                            content = stepOrContainer.content,
                            traits = listOf(),
                            actions = hashMapOf(),
                            type = stepOrContainer.type,
                        )
                    ),
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
