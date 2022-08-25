package com.appcues.data.mapper.step

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.map
import com.appcues.data.mapper.styling.mapComponentContentMode
import com.appcues.data.mapper.styling.mapComponentSize
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.BoxPrimitive
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.ExperiencePrimitive.EmbedHtmlPrimitive
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive.OptionItem
import com.appcues.data.model.ExperiencePrimitive.TextInputPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.data.model.styling.ComponentControlPosition
import com.appcues.data.model.styling.ComponentDataType
import com.appcues.data.model.styling.ComponentDisplayFormat
import com.appcues.data.model.styling.ComponentDistribution
import com.appcues.data.model.styling.ComponentSelectMode
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

internal fun PrimitiveResponse.mapPrimitive(): ExperiencePrimitive {
    return when (this) {
        is StackPrimitiveResponse -> mapStackPrimitive()
        is BoxPrimitiveResponse -> mapBoxPrimitive()
        is TextPrimitiveResponse -> mapTextPrimitive()
        is ButtonPrimitiveResponse -> mapButtonPrimitive()
        is ImagePrimitiveResponse -> mapImagePrimitive()
        is EmbedPrimitiveResponse -> mapEmbedPrimitive()
        is BlockPrimitiveResponse -> content.mapPrimitive()
        is OptionSelectPrimitiveResponse -> mapOptionSelectPrimitive()
        is TextInputPrimitiveResponse -> mapTextInputPrimitive()
    }
}
private fun TextPrimitiveResponse.mapTextPrimitive() = TextPrimitive(
    id = id,
    text = text,
    style = style.map(),
)

private fun BoxPrimitiveResponse.mapBoxPrimitive() = BoxPrimitive(
    id = id,
    items = items.map { it.mapPrimitive() },
    style = style.map(),
)

private fun ButtonPrimitiveResponse.mapButtonPrimitive() = ButtonPrimitive(
    id = id,
    content = content.mapPrimitive(),
    style = style.map(),
)

private fun EmbedPrimitiveResponse.mapEmbedPrimitive() = EmbedHtmlPrimitive(
    id = id,
    style = style.map(),
    embed = embed,
    intrinsicSize = intrinsicSize.mapComponentSize(),
)

private fun ImagePrimitiveResponse.mapImagePrimitive() = ImagePrimitive(
    id = id,
    url = imageUrl,
    accessibilityLabel = accessibilityLabel,
    style = style.map(),
    intrinsicSize = intrinsicSize.mapComponentSize(),
    contentMode = mapComponentContentMode(contentMode),
    blurHash = blurHash,
)

private fun StackPrimitiveResponse.mapStackPrimitive() = when (orientation) {
    "vertical" -> mapVerticalStack()
    "horizontal" -> mapHorizontalStack()
    else -> throw AppcuesMappingException("stack($id) unknown orientation $orientation")
}

private fun StackPrimitiveResponse.mapVerticalStack(): VerticalStackPrimitive {
    return VerticalStackPrimitive(
        id = id,
        items = items.map { it.mapPrimitive() },
        spacing = spacing,
        style = style.map(),
    )
}

private fun StackPrimitiveResponse.mapHorizontalStack(): HorizontalStackPrimitive {
    return HorizontalStackPrimitive(
        id = id,
        items = items.map { it.mapPrimitive() },
        distribution = mapComponentDistribution(distribution),
        spacing = spacing,
        style = style.map(),
    )
}

private fun mapComponentDistribution(value: String?) = when (value) {
    "center" -> ComponentDistribution.CENTER
    "equal" -> ComponentDistribution.EQUAL
    else -> ComponentDistribution.CENTER
}

private fun TextInputPrimitiveResponse.mapTextInputPrimitive() = TextInputPrimitive(
    id = id,
    style = style.map(),
    label = label.mapTextPrimitive(),
    placeholder = placeholder,
    defaultValue = defaultValue,
    required = required ?: false,
    numberOfLines = numberOfLines ?: 1,
    maxLength = maxLength,
    dataType = mapComponentDataType(dataType),
    textFieldStyle = textFieldStyle.map(),
)

private fun mapComponentDataType(value: String?) = when (value) {
    "text" -> ComponentDataType.TEXT
    "number" -> ComponentDataType.NUMBER
    "email" -> ComponentDataType.EMAIL
    "phone" -> ComponentDataType.PHONE
    "name" -> ComponentDataType.NAME
    "address" -> ComponentDataType.ADDRESS
    else -> ComponentDataType.TEXT
}

private fun OptionSelectPrimitiveResponse.mapOptionSelectPrimitive() = OptionSelectPrimitive(
    id = id,
    style = style.map(),
    label = label.mapTextPrimitive(),
    selectMode = mapComponentSelectMode(selectMode),
    options = options.map { OptionItem(it.value, it.content.mapPrimitive(), it.selectedContent?.mapPrimitive()) },
    defaultValue = defaultValue ?: listOf(),
    required = required ?: false,
    controlPosition = mapComponentControlPosition(controlPosition),
    displayFormat = mapComponentDisplayFormat(displayFormat),
)

private fun mapComponentSelectMode(value: String) = when (value) {
    "single" -> ComponentSelectMode.SINGLE
    "multi" -> ComponentSelectMode.MULTIPLE
    else -> ComponentSelectMode.SINGLE
}

private fun mapComponentControlPosition(value: String?) = when (value) {
    "leading" -> ComponentControlPosition.LEADING
    "trailing" -> ComponentControlPosition.TRAILING
    "top" -> ComponentControlPosition.TOP
    "bottom" -> ComponentControlPosition.BOTTOM
    "hidden" -> ComponentControlPosition.HIDDEN
    else -> ComponentControlPosition.LEADING
}

private fun mapComponentDisplayFormat(value: String?) = when (value) {
    "verticalList" -> ComponentDisplayFormat.VERTICAL_LIST
    "horizontalList" -> ComponentDisplayFormat.HORIZONTAL_LIST
    "picker" -> ComponentDisplayFormat.PICKER
    else -> ComponentDisplayFormat.VERTICAL_LIST
}
