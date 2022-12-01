package com.appcues.data.remote.adapters

import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.data.remote.response.experience.FailedExperienceResponse
import com.appcues.data.remote.response.experience.LossyExperienceResponse
import com.appcues.data.remote.response.experience.UnknownExperienceResponse
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

// This is a custom adapter that will allow us to lossy decode the array of Experiences that come back in a qualification response.
// The idea is that we don't want to fail en entire response if a single Experience fails to decode, if there are other Experiences
// that could still be qualified and render to the user.
internal class LossyExperienceResponseAdapter {

    @FromJson
    fun fromJson(
        reader: JsonReader,
        delegate: JsonAdapter<ExperienceResponse>,
        failureDelegate: JsonAdapter<FailedExperienceResponse>
    ): LossyExperienceResponse {
        val value = reader.readJsonValue()
        try {
            // 1. try to decode the ExperienceResponse as normal - success case
            delegate.fromJsonValue(value)?.let { return it }
        } catch (exception: JsonDataException) {
            try {
                // 2. try to decode a minimal response for failures that provides enough context to report flow issues,
                //    passing through the error message from the initial failed deserialization
                failureDelegate.fromJsonValue(value)?.let { return it.apply { error = exception.message } }
            } catch (exception: JsonDataException) {
                // 3. fallback - completely unknown value in the JSON we cannot parse
                return UnknownExperienceResponse
            }
        }
        // 4. not expected to be reached, but would occur if a fromJsonValue call above succeeded, but produced a null value
        return UnknownExperienceResponse
    }

    @ToJson
    @Suppress("UnusedPrivateMember", "UNUSED_PARAMETER") // required by Moshi
    fun toJson(value: LossyExperienceResponse): String {
        throw UnsupportedOperationException("experiences only support deserialization")
    }
}
