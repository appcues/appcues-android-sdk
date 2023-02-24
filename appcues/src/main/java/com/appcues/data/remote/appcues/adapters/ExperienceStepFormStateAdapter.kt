package com.appcues.data.remote.appcues.adapters

import com.appcues.data.model.ExperienceStepFormState
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.ToJson

internal class ExperienceStepFormStateAdapter {

    @JsonClass(generateAdapter = true)
    internal data class StubExperienceStepFormState(
        val value: String,
    )

    @FromJson
    @Suppress("UnusedPrivateMember", "UNUSED_PARAMETER") // required by Moshi
    fun fromJson(formState: StubExperienceStepFormState): ExperienceStepFormState {
        throw UnsupportedOperationException("step container only supports serialization")
    }

    @ToJson
    fun toJson(formState: ExperienceStepFormState): Map<String, Any> {
        return formState.toHashMap()
    }
}
