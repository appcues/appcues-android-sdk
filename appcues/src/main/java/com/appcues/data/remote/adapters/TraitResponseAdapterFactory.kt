package com.appcues.data.remote.adapters

import com.appcues.data.remote.response.trait.TraitResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.Type

class TraitResponseAdapterFactory : JsonAdapter.Factory {

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return if (
            Types.getRawType(type) == List::class.java &&
            Types.collectionElementType(type, List::class.java) == TraitResponse::class.java
        ) {
            val delegate = moshi.adapter(TraitResponse::class.java)
            TraitResponseAdapter(delegate)
        } else {
            null
        }
    }

    private class TraitResponseAdapter(
        private val delegate: JsonAdapter<TraitResponse>,
    ) : JsonAdapter<List<TraitResponse>>() {

        override fun fromJson(reader: JsonReader): List<TraitResponse> {
            val traits = mutableListOf<TraitResponse>()
            val traitTypes = mutableSetOf<String>()
            val duplicates = mutableSetOf<String>()

            reader.beginArray()

            while (reader.hasNext()) {
                val value = reader.readJsonValue()
                delegate.fromJsonValue(value)?.let {
                    if (!traitTypes.add(it.type)) {
                        // If adding the type to the existing set returns false, this means it
                        // was already in the set, and this is a duplicate type in this collection.
                        // Duplicates are collected and used in a decoding error.
                        duplicates.add(it.type)
                    } else {
                        // Normal case, capture the decoded trait for our resulting list
                        traits.add(it)
                    }
                }
            }

            if (duplicates.isNotEmpty()) {
                val message = "multiple traits of same type are not supported: ${duplicates.joinToString(",")}. " +
                    "Found at path ${reader.path}"
                throw JsonDataException(message)
            }

            reader.endArray()
            return traits
        }

        override fun toJson(writer: JsonWriter, value: List<TraitResponse>?) {
            throw UnsupportedOperationException("trait responses only support deserialization")
        }
    }
}
