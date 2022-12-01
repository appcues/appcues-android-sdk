package com.appcues.data.remote.adapters

import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.data.remote.response.experience.FailedExperienceResponse
import com.appcues.data.remote.response.experience.LossyExperienceResponse
import com.appcues.data.remote.response.experience.UnknownExperienceResponse
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import com.appcues.util.doIfFailure
import com.appcues.util.doIfSuccess
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
        // this is the item we are decoding, expected to be a well formed Experience response
        val value = reader.readJsonValue()

        // 1. try to decode the ExperienceResponse as normal - success case
        val experienceDecodingResult = decode(value, delegate)
        experienceDecodingResult.doIfSuccess {
            return it
        }

        // 2. try to decode a minimal response for failures that provides enough context to report flow issues (experience id),
        //    passing through the error message from the initial failed deserialization
        experienceDecodingResult.doIfFailure { error ->
            decode(value, failureDelegate).doIfSuccess {
                return it.apply { this.error = error }
            }
        }

        // 3. fallback - completely unknown value in the JSON we cannot parse
        return UnknownExperienceResponse
    }

    private fun <T : Any> decode(value: Any?, adapter: JsonAdapter<T>): ResultOf<T, String?> {
        var error: String? = null
        try {
            adapter.fromJsonValue(value)?.let { return Success(it) }
        } catch (exception: JsonDataException) {
            error = exception.message
        }
        return Failure(error)
    }

    @ToJson
    @Suppress("UnusedPrivateMember", "UNUSED_PARAMETER") // required by Moshi
    fun toJson(value: LossyExperienceResponse): String {
        throw UnsupportedOperationException("experiences only support deserialization")
    }
}
