package com.appcues.data

import com.appcues.data.remote.adapters.DateAdapter
import com.appcues.data.remote.adapters.StepContainerAdapter
import com.appcues.data.remote.adapters.UUIDAdapter
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.BlockPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.BoxPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.ButtonPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.EmbedPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.ImagePrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.OptionSelectPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.StackPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextInputPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

internal object MoshiConfiguration {

    val moshi: Moshi = Moshi.Builder()
        .add(DateAdapter())
        .add(UUIDAdapter())
        .add(getPrimitiveFactory())
        .add(StepContainerAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    inline fun <reified T : Any> fromAny(any: Any?): T? {
        val adapter = moshi.adapter(T::class.java)
        val anyAdapter = moshi.adapter<Any>(Object::class.java)
        return adapter.run {
            fromJson(anyAdapter.toJson(any))
        }
    }

    private fun getPrimitiveFactory() : JsonAdapter.Factory {
        return PolymorphicJsonAdapterFactory.of(PrimitiveResponse::class.java, "type")
            .withSubtype(BoxPrimitiveResponse::class.java, PrimitiveResponse.Type.BOX.jsonName)
            .withSubtype(ButtonPrimitiveResponse::class.java, PrimitiveResponse.Type.BUTTON.jsonName)
            .withSubtype(EmbedPrimitiveResponse::class.java, PrimitiveResponse.Type.EMBED.jsonName)
            .withSubtype(ImagePrimitiveResponse::class.java, PrimitiveResponse.Type.IMAGE.jsonName)
            .withSubtype(OptionSelectPrimitiveResponse::class.java, PrimitiveResponse.Type.OPTION_SELECT.jsonName)
            .withSubtype(StackPrimitiveResponse::class.java, PrimitiveResponse.Type.STACK.jsonName)
            .withSubtype(TextInputPrimitiveResponse::class.java, PrimitiveResponse.Type.TEXT_INPUT.jsonName)
            .withSubtype(TextPrimitiveResponse::class.java, PrimitiveResponse.Type.TEXT.jsonName)
            .withSubtype(BlockPrimitiveResponse::class.java, PrimitiveResponse.Type.BLOCK.jsonName)
    }
}
