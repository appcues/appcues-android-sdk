package com.appcues.data.remote.adapters

import com.appcues.data.model.ExperienceStepFormState
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson
import java.util.UUID

@JsonClass(generateAdapter = true)
internal data class ExperienceStepFormStateRequestItem(
    val fieldId: UUID,
    val fieldType: String,
    val fieldRequired: Boolean,
    val label: String,
    val value: String,
)

internal class ExperienceStepFormStateAdapter {
    @FromJson
    @Suppress("UnusedPrivateMember", "UNUSED_PARAMETER") // required by Moshi
    fun fromJson(formState: ExperienceStepFormStateRequestItem): ExperienceStepFormState {
        throw UnsupportedOperationException("step container only supports serialization")
    }

    @ToJson
    fun toJson(formState: ExperienceStepFormState): Map<String, List<ExperienceStepFormStateRequestItem>> {
        return hashMapOf(
            "formResponse" to formState.formItems.map {
                ExperienceStepFormStateRequestItem(
                    fieldId = it.id,
                    fieldType = it.type,
                    fieldRequired = it.isRequired,
                    label = it.label,
                    value = it.value,
                )
            }
        )
    }
}
