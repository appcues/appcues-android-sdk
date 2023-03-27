package com.appcues.data.model.styling

internal data class ComponentStyle(
    // General properties
    val width: Double? = null,
    val height: Double? = null,
    val marginLeading: Double? = null,
    val marginTop: Double? = null,
    val marginTrailing: Double? = null,
    val marginBottom: Double? = null,
    val paddingLeading: Double? = null,
    val paddingTop: Double? = null,
    val paddingTrailing: Double? = null,
    val paddingBottom: Double? = null,
    val cornerRadius: Double? = null,
    val foregroundColor: ComponentColor? = null,
    val backgroundColor: ComponentColor? = null,
    val backgroundGradient: List<ComponentColor>? = null,
    val backgroundImage: ComponentBackgroundImage? = null,
    val borderColor: ComponentColor? = null,
    val borderWidth: Double? = null,
    val shadow: ComponentShadow? = null,

    // Text related properties
    val fontName: String? = null,
    val fontSize: Double? = null,
    val letterSpacing: Double? = null,
    val lineHeight: Double? = null,
    val textAlignment: ComponentHorizontalAlignment? = null,

    // Container related properties
    val verticalAlignment: ComponentVerticalAlignment? = null,
    val horizontalAlignment: ComponentHorizontalAlignment? = null,
) {

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
