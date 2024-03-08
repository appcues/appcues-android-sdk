package com.appcues.ui.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Typeface
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment
import com.appcues.ui.composables.LocalLogcues
import com.appcues.ui.composables.LocalPackageNames
import java.io.File

internal fun ComponentStyle?.getMargins(defaultValue: Dp = 0.dp): PaddingValues {
    return if (this == null) PaddingValues(defaultValue) else
        PaddingValues(
            start = marginLeading?.dp ?: defaultValue,
            top = marginTop?.dp ?: defaultValue,
            bottom = marginBottom?.dp ?: defaultValue,
            end = marginTrailing?.dp ?: defaultValue,
        )
}

internal fun ComponentStyle?.getPaddings(defaultValue: Dp = 0.dp): PaddingValues {
    return if (this == null) PaddingValues(defaultValue) else
        PaddingValues(
            start = paddingLeading?.dp ?: defaultValue,
            bottom = paddingBottom?.dp ?: defaultValue,
            top = paddingTop?.dp ?: defaultValue,
            end = paddingTrailing?.dp ?: defaultValue,
        )
}

internal fun ComponentStyle.getCornerRadius(defaultValue: Dp = 0.dp): Dp {
    return cornerRadius?.dp ?: defaultValue
}

internal fun ComponentStyle.getTextAlignment(): TextAlign? {
    return when (textAlignment) {
        ComponentHorizontalAlignment.LEADING -> TextAlign.Start
        ComponentHorizontalAlignment.CENTER -> TextAlign.Center
        ComponentHorizontalAlignment.TRAILING -> TextAlign.End
        null -> null
    }
}

@Composable
internal fun ComponentStyle.getFontFamily(): FontFamily? {
    val context = LocalContext.current
    val packageNames = LocalPackageNames.current
    val fontFamily = FontFamily.getSystemFontFamily(fontName)
        ?: FontFamily.getFontResource(context, packageNames, fontName)
        ?: FontFamily.getFontAsset(context, fontName)
        ?: FontFamily.getSystemFont(fontName)

    val logcues = LocalLogcues.current
    LaunchedEffect(Unit) {
        if (fontName != null && fontFamily == null) {
            logcues.error(
                "Font \"$fontName\" not found. Make sure to place it in your main app " +
                    "package or in one of the provided packageNames during SDK initialization"
            )
        }
    }
    
    return fontFamily
}

// handle system fonts available in Compose with variable weight
// convention is name of: "System {FamilyName} {Weight}"
private fun FontFamily.Companion.getSystemFontFamily(fontName: String?): FontFamily? {
    if (fontName != null && fontName.lowercase().startsWith("system ")) {
        val tokens = fontName.split(" ")
        if (tokens.count() > 1) {
            val systemFontFamily = tokens[1]
            return FontFamily.get(systemFontFamily)
        }
    }
    return null
}

private fun FontFamily.Companion.getFontResource(context: Context, packageNames: List<String>, fontName: String?): FontFamily? {
    // Font resources are only available on API 26+ (Android 8.0, "O"), so don't even try on lower versions
    if (VERSION.SDK_INT >= VERSION_CODES.O && fontName != null) {
        val mergedPackageNames = mutableSetOf<String>().apply {
            add(context.packageName)
            addAll(packageNames)
        }

        // iterate the packageNames list and return the first not null font
        mergedPackageNames.forEach {
            val font = getFontResource(context, it, fontName)
            if (font != null) return font
        }
    }

    // font was not found in resources
    return null
}

// try to load a custom font from a resource in the host application
@RequiresApi(VERSION_CODES.O)
@SuppressLint("DiscouragedApi")
private fun FontFamily.Companion.getFontResource(context: Context, packageName: String, fontName: String): FontFamily? {
    // this is an attempt to provide convenience to convert to valid resource names, for ex. if a font
    // name of "Lato-Black" was used, it will auto convert to "lato_black" resource name
    val resourceName = fontName.lowercase().replace("-", "_")
    val fontId = context.resources.getIdentifier(resourceName, "font", packageName)
    if (fontId != 0) {
        // Even if the font identifier is found, this typeface may not be available on the current
        // OS level, if any api-specific resource folders have been used, i.e. "font-v30". We do
        // another check here to try and catch this, since otherwise it will result in an uncaught
        // exception during Compose render time. That exception will crash the app when
        // trying to show the Appcues experience.
        @Suppress("SwallowedException")
        return try {
            // the result of this getFont is unused, but success indicates the resource is available
            // and we can progress with the creation of the Font and FontFamily
            context.resources.getFont(fontId)
            FontFamily(Font(fontId))
        } catch (_: NotFoundException) {
            null
        } catch (_: NullPointerException) {
            // this can happen in our snapshot unit tests when the font is not available
            null
        }
    }
    return null
}

// try to load a custom font from an embedded asset in the host application
private fun FontFamily.Companion.getFontAsset(context: Context, fontName: String?): FontFamily? {
    if (fontName != null) {
        val assetName = "$fontName.ttf"
        val fontsInAssets = context.assets.list("fonts")
        if (fontsInAssets != null && fontsInAssets.contains(assetName)) {
            val typeface = Typeface.createFromAsset(context.assets, "fonts/$fontName.ttf")
            if (typeface != null) {
                return FontFamily(typeface)
            }
        }
    }
    return null
}

// try to load an individual font file from the Android system path
private fun FontFamily.Companion.getSystemFont(fontName: String?): FontFamily? {
    if (fontName != null) {
        val file = File("/system/fonts/$fontName.ttf")
        if (file.exists()) {
            val typeface = Typeface.createFromFile(file)
            if (typeface != null) {
                return FontFamily(typeface)
            }
        }
    }
    return null
}

internal fun FontFamily.Companion.get(name: String): FontFamily {
    return when (name.lowercase()) {
        "serif" -> Serif
        "sansserif" -> SansSerif
        "monospaced" -> Monospace
        "cursive" -> Cursive
        else -> Default
    }
}

internal fun FontWeight.Companion.get(name: String): FontWeight {
    // NOTE: naming conventions here match iOS, rather than Android system defaults
    // for ease of use in the Appcues builder across platforms
    return when (name.lowercase()) {
        // 100
        "ultralight" -> Thin
        // 200
        "thin" -> ExtraLight
        // 300
        "light" -> Light
        // 500
        "medium" -> Medium
        // 600
        "semibold" -> SemiBold
        // 700
        "bold" -> Bold
        // 800
        "heavy" -> ExtraBold
        // 900
        "black" -> Black
        // 400
        else -> Normal
    }
}

internal fun ComponentStyle.getFontWeight(): FontWeight? {
    // font weight only supported on system fonts with naming convention
    // convention is name of: "System {FamilyName} {Weight}"
    if (fontName != null) {
        if (fontName.lowercase().startsWith("system ")) {
            val tokens = fontName.split(" ")
            if (tokens.count() > 2) {
                val systemFontWeight = tokens[2]
                return FontWeight.get(systemFontWeight)
            }
        }
    }
    return null
}

internal fun ComponentStyle.getVerticalAlignment(default: Alignment.Vertical): Alignment.Vertical {
    return when (verticalAlignment) {
        ComponentVerticalAlignment.TOP -> Alignment.Top
        ComponentVerticalAlignment.CENTER -> Alignment.CenterVertically
        ComponentVerticalAlignment.BOTTOM -> Alignment.Bottom
        null -> default
    }
}

internal fun ComponentStyle.getHorizontalAlignment(default: Alignment.Horizontal): Alignment.Horizontal {
    return when (horizontalAlignment) {
        ComponentHorizontalAlignment.LEADING -> Alignment.Start
        ComponentHorizontalAlignment.CENTER -> Alignment.CenterHorizontally
        ComponentHorizontalAlignment.TRAILING -> Alignment.End
        null -> default
    }
}

internal fun ComponentStyle?.getBoxAlignment(): Alignment {
    if (this == null) return Alignment.Center

    return getBoxAlignment(horizontalAlignment, verticalAlignment)
}

internal fun getBoxAlignment(
    horizontalAlignment: ComponentHorizontalAlignment?,
    verticalAlignment: ComponentVerticalAlignment?
): Alignment {
    val horizontalBias = when (horizontalAlignment) {
        ComponentHorizontalAlignment.LEADING -> -1f
        ComponentHorizontalAlignment.CENTER -> 0f
        ComponentHorizontalAlignment.TRAILING -> 1f
        null -> 0f
    }
    val verticalBias = when (verticalAlignment) {
        ComponentVerticalAlignment.TOP -> -1f
        ComponentVerticalAlignment.CENTER -> 0f
        ComponentVerticalAlignment.BOTTOM -> 1f
        null -> 0f
    }

    return BiasAlignment(horizontalBias, verticalBias)
}

@Composable
internal fun ComponentStyle.getTextStyle(): TextStyle {
    return TextStyle(
        color = foregroundColor.getColor(isSystemInDarkTheme()) ?: Color.Unspecified,
        fontSize = fontSize?.sp ?: TextUnit.Unspecified,
        lineHeight = lineHeight?.sp ?: TextUnit.Unspecified,
        textAlign = getTextAlignment() ?: TextAlign.Unspecified,
        fontFamily = getFontFamily(),
        letterSpacing = letterSpacing?.sp ?: TextUnit.Unspecified,
        fontWeight = getFontWeight(),
    )
}
