package com.appcues.data.remote.appcues.adapters

import com.appcues.data.remote.appcues.response.experience.ExperienceResponse
import com.appcues.data.remote.appcues.response.experience.FailedExperienceResponse
import com.appcues.data.remote.appcues.response.experience.LossyExperienceResponse
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import com.appcues.util.doIfFailure
import com.appcues.util.doIfSuccess
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

internal class LossyExperienceResponseAdapterFactory : JsonAdapter.Factory {

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return if (
            Types.getRawType(type) == List::class.java &&
            Types.collectionElementType(type, List::class.java) == LossyExperienceResponse::class.java
        ) {
            val delegate = moshi.adapter(ExperienceResponse::class.java)
            val failureDelegate = moshi.adapter(FailedExperienceResponse::class.java)
            LossyExperienceResponseAdapter(delegate, failureDelegate)
        } else {
            null
        }
    }

    // This is a custom adapter that will allow us to lossy decode the array of Experiences that come back in a qualification response.
    // The idea is that we don't want to fail en entire response if a single Experience fails to decode, if there are other Experiences
    // that could still be qualified and render to the user.
    private class LossyExperienceResponseAdapter(
        private val delegate: JsonAdapter<ExperienceResponse>,
        private val failureDelegate: JsonAdapter<FailedExperienceResponse>,
    ) : JsonAdapter<List<LossyExperienceResponse>>() {

        override fun fromJson(reader: JsonReader): List<LossyExperienceResponse> {
            val experiences = mutableListOf<LossyExperienceResponse>()

            // this is the item we are decoding, expected to be a well formed Experience response
            reader.beginArray()

            while (reader.hasNext()) {

                val value = reader.readJsonValue()

                // 1. try to decode the ExperienceResponse as normal - success case
                val experienceDecodingResult = decode(value, delegate)
                experienceDecodingResult.doIfSuccess {
                    experiences.add(it)
                }

                // 2. try to decode a minimal response for failures that provides enough context to report flow issues (experience id),
                //    passing through the error message from the initial failed deserialization
                experienceDecodingResult.doIfFailure { error ->
                    decode(value, failureDelegate).doIfSuccess {
                        experiences.add(it.apply { this.error = "Error parsing Experience JSON data: $error" })
                    }
                }
            }

            reader.endArray()
            return experiences
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

        override fun toJson(writer: JsonWriter, value: List<LossyExperienceResponse>?) {
            throw UnsupportedOperationException("experiences only support deserialization")
        }
    }
}
