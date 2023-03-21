package com.appcues.data.remote.appcues.adapters

import com.appcues.Appcues
import com.appcues.ElementSelector
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson

// A Moshi Adapter that assists with serialization of selector data to/from string mappings,
// based on the current element targeting strategy in use.
internal class ElementSelectorAdapter {

    @FromJson
    fun fromJson(properties: Map<String, String>): ElementSelector {
        return Appcues.elementTargeting.inflateSelectorFrom(properties)
            ?: throw JsonDataException("Cannot create element selector from $properties")
    }

    @ToJson
    fun toJson(value: ElementSelector): Map<String, String> {
        return value.toMap()
    }
}
