package com.appcues.data.model.styling

internal data class ComponentStyle(
    // General properties
    val width: Double? = null,
    val height: Double? = null,
    val marginLeading: Double = 0.0,
    val marginTop: Double = 0.0,
    val marginTrailing: Double = 0.0,
    val marginBottom: Double = 0.0,
    val paddingLeading: Double = 0.0,
    val paddingTop: Double = 0.0,
    val paddingTrailing: Double = 0.0,
    val paddingBottom: Double = 0.0,
    val cornerRadius: Double = 0.0,
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
