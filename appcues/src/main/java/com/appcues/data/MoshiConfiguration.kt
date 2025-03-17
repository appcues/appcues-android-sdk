package com.appcues.data

import com.appcues.data.remote.adapters.DateAdapter
import com.appcues.data.remote.adapters.UUIDAdapter
import com.appcues.data.remote.appcues.adapters.ElementSelectorAdapter
import com.appcues.data.remote.appcues.adapters.ExperienceStepFormStateAdapter
import com.appcues.data.remote.appcues.adapters.LossyExperienceResponseAdapterFactory
import com.appcues.data.remote.appcues.adapters.StepContainerAdapter
import com.appcues.data.remote.appcues.adapters.TraitResponseAdapterFactory
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.BlockPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.BoxPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.ButtonPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.CustomComponentPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.EmbedPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.ImagePrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.OptionSelectPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.SpacerPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.StackPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.TextInputPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonAdapter.Factory
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.lang.reflect.Type
import kotlin.annotation.AnnotationRetention.RUNTIME

internal object MoshiConfiguration {

    // can be used to add a @SerializeNull annotation on any optional property in a model that is desired
    // to have a null value output in JSON rather than the property being omitted. Adapted from
    // https://stackoverflow.com/a/52265735
    @Retention(RUNTIME)
    @JsonQualifier
    annotation class SerializeNull {

        companion object {

            val JSON_ADAPTER_FACTORY = object : Factory {
                override fun create(type: Type, annotations: Set<Annotation?>, moshi: Moshi): JsonAdapter<*>? {
                    val nextAnnotations = Types.nextAnnotations(
                        annotations,
                        SerializeNull::class.java
                    ) ?: return null
                    return moshi.nextAdapter<Any>(this, type, nextAnnotations).serializeNulls()
                }
            }
        }
    }

    val moshi: Moshi = Moshi.Builder()
        .add(DateAdapter())
        .add(UUIDAdapter())
        .add(getPrimitiveFactory())
        .add(SerializeNull.JSON_ADAPTER_FACTORY)
        .add(StepContainerAdapter())
        .add(ExperienceStepFormStateAdapter())
        .add(ElementSelectorAdapter())
        .add(LossyExperienceResponseAdapterFactory())
        .add(TraitResponseAdapterFactory())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    inline fun <reified T : Any> fromAny(any: Any?): T? {
        val adapter = moshi.adapter(T::class.java)
        val anyAdapter = moshi.adapter<Any>(Object::class.java)
        return adapter.run {
            try {
                fromJson(anyAdapter.toJson(any))
            } catch (exception: JsonDataException) {
                null
            }
        }
    }

    private fun getPrimitiveFactory(): Factory {
        return PolymorphicJsonAdapterFactory.of(PrimitiveResponse::class.java, "type")
            .withSubtype(BoxPrimitiveResponse::class.java, "box")
            .withSubtype(CustomComponentPrimitiveResponse::class.java, "customComponent")
            .withSubtype(ButtonPrimitiveResponse::class.java, "button")
            .withSubtype(EmbedPrimitiveResponse::class.java, "embed")
            .withSubtype(ImagePrimitiveResponse::class.java, "image")
            .withSubtype(OptionSelectPrimitiveResponse::class.java, "optionSelect")
            .withSubtype(StackPrimitiveResponse::class.java, "stack")
            .withSubtype(TextInputPrimitiveResponse::class.java, "textInput")
            .withSubtype(TextPrimitiveResponse::class.java, "text")
            .withSubtype(BlockPrimitiveResponse::class.java, "block")
            .withSubtype(SpacerPrimitiveResponse::class.java, "spacer")
    }
}
