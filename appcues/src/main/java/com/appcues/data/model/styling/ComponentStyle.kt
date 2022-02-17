package com.appcues.data.model.styling

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ComponentStyle(
    // General properties
    val width: Int? = null,
    val height: Int? = null,
    val marginLeading: Int = 0,
    val marginTop: Int = 0,
    val marginTrailing: Int = 0,
    val marginBottom: Int = 0,
    val paddingLeading: Int = 0,
    val paddingTop: Int = 0,
    val paddingTrailing: Int = 0,
    val paddingBottom: Int = 0,
    val cornerRadius: Int = 0,
    val foregroundColor: ComponentColor? = null,
    val backgroundColor: ComponentColor? = null,
    val backgroundGradient: List<ComponentColor>? = null,
    val borderColor: ComponentColor? = null,
    val borderWidth: Int? = null,
    val shadow: ComponentShadow? = null,

    // Text related properties
    val fontName: String? = null,
    val fontSize: Int? = null,
    val fontWeight: ComponentFontWeight? = null,
    val letterSpacing: Int? = null,
    val lineHeight: Int? = null,
    val textAlignment: ComponentHorizontalAlignment? = null,

    // Container related properties
    val verticalAlignment: ComponentVerticalAlignment? = null,
    val horizontalAlignment: ComponentHorizontalAlignment? = null,

    // missing properties: backgroundGradient, shadow
) : Parcelable {

    enum class ComponentFontWeight {
        ULTRA_LIGHT, THIN, LIGHT, REGULAR, MEDIUM, SEMI_BOLD, BOLD, HEAVY, BLACK
    }

    enum class ComponentHorizontalAlignment {
        LEADING, CENTER, TRAILING
    }

    enum class ComponentVerticalAlignment {
        TOP, CENTER, BOTTOM
    }
}
