package com.appcues.data.mapper.styling

import com.appcues.data.mapper.AppcuesMappingException

private val colorHexRegex = "#?([0-9a-f]{3,8})".toRegex(RegexOption.IGNORE_CASE)

private const val LENGTH_RGB_ONE_DIGIT = 3
private const val LENGTH_RGB_TWO_DIGITS = 6
private const val LENGTH_RGBA_ONE_DIGIT = 4
private const val LENGTH_RGBA_TWO_DIGITS = 8

@Suppress("MagicNumber")
internal fun normalizeToArgbLong(rgbaHex: String): Long {
    return normalizeAsRGBA(rgbaHex).toLong(radix = 16).run {
        (((this shr 0 and 0xFF) shl 24) or (this shr 8))
    }
}

private fun normalizeAsRGBA(value: String): String {
    val matchResult = colorHexRegex.matchEntire(value)
        ?: throw AppcuesMappingException("The provided colorHex '$value' is not a valid color hex")

    val hexValue = matchResult.groups[1]!!.value.lowercase()
    return when (hexValue.length) {
        LENGTH_RGB_ONE_DIGIT -> expandToTwoDigitsPerComponent("${hexValue}f")
        LENGTH_RGB_TWO_DIGITS -> "${hexValue}ff"
        LENGTH_RGBA_ONE_DIGIT -> expandToTwoDigitsPerComponent(hexValue)
        LENGTH_RGBA_TWO_DIGITS -> hexValue
        else -> throw AppcuesMappingException("The provided colorHex '$value' is not in a supported format")
    }
}

private fun expandToTwoDigitsPerComponent(hexValue: String) = hexValue.asSequence()
    .map { "$it$it" }
    .reduce { accumulatedHex, component -> accumulatedHex + component }
