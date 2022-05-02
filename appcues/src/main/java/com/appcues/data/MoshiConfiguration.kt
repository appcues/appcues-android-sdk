package com.appcues.data

import com.appcues.data.remote.adapters.DateAdapter
import com.appcues.data.remote.adapters.StepContainerAdapter
import com.appcues.data.remote.adapters.UUIDAdapter
import com.squareup.moshi.Moshi

internal object MoshiConfiguration {

    val moshi: Moshi = Moshi.Builder()
        .add(DateAdapter())
        .add(UUIDAdapter())
        .add(StepContainerAdapter())
        .build()

    inline fun <reified T : Any> fromAny(any: Any?): T? {
        val adapter = moshi.adapter(T::class.java)
        val anyAdapter = moshi.adapter<Any>(Object::class.java)
        return adapter.run {
            fromJson(anyAdapter.toJson(any))
        }
    }
}
