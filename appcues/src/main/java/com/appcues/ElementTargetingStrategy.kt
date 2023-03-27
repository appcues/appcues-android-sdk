package com.appcues

import android.view.View
import com.squareup.moshi.JsonClass
import java.util.UUID

/**
 * Defines an element targeting strategy type, which can be used to capture view layout
 * information in the application UI as well as render element targeted experiences.
 */
interface ElementTargetingStrategy {

    /**
     * Capture the layout hierarchy in the currently rendered screen of the application.
     *
     * @return: Root view element for the current screen, or null if not available. The view element
     *          contains sub-views recursively in the `children` property.
     */
    fun captureLayout(): ViewElement?

    /**
     * Create and return a selector implementation from configuration properties.
     * @param properties Key-value collection of properties to identify a given view element.
     * @return the ElementSelector implementation for this element targeting strategy, based on
     *         the given properties. If no applicable properties are found, the return value should be `nil`.
     */
    fun inflateSelectorFrom(properties: Map<String, String>): ElementSelector?
}

/**
 * Defines a type to identify view elements with a set of selector properties.
 */
interface ElementSelector {

    /**
     * Exports the selector properties into a string mapping for serialization.
     *
     * @return Map of string key-value pairs for selector properties.
     */
    fun toMap(): Map<String, String>

    /**
     * Evaluate how closely this selector matches with the given target selector.
     *
     * Any value greater than zero means there is a match. The higher the value, the more exact the match.
     * In the cases of selectors with multiple identifying attributes, there may be partial matches with lower values,
     * and exact matches with higher values. Any negative value indicates no match.
     *
     * @return Value for the quality of selector match.
     */
    fun evaluateMatch(target: ElementSelector): Int
}

/**
 * Represents a view in the layout hierarchy of the application.
 *
 * The view information provided in this structure provides screen capture metadata to Appcues
 * servers that can be used for element targeted experiences. The layout information is used by the
 * Appcues Mobile Builder to select and target UI elements, and used by the Appcues Android SDK to
 * position experience content relative to targeted elements.
 */
@JsonClass(generateAdapter = true)
data class ViewElement(
    /**
     * Auto-generated unique ID for the view.
     */
    val id: UUID = UUID.randomUUID(),

    /**
     * The x-coordinate for view position, with origin in the upper-left corner.
     * This value is in screen coordinates, not relative to the parent.
     */
    val x: Int,

    /**
     * The y-coordinate for view position, with origin is in the upper-left corner.
     * This value is in screen coordinates, not relative to the parent.
     */
    val y: Int,

    /**
     * The width of the view.
     */
    val width: Int,

    /**
     * The height of the view.
     */
    val height: Int,

    /**
     * A value representing the type of the view.
     */
    val type: String,

    /**
     * The element selector details that can be used to target content to this view. The selector structure
     * depends on the UI toolkit in use. If no identifiable properties exist for this view, this selector value should be null.
     */
    val selector: ElementSelector?,

    /**
     * The sub-views contained within this view, if any.
     */
    val children: List<ViewElement>?,
)

fun View.isAppcuesView(): Boolean {
    return this.id == R.id.appcues_debugger_view
}

internal fun ElementTargetingStrategy.findMatches(selector: ElementSelector): List<Pair<ViewElement, Int>>? {
    return captureLayout()?.viewsMatching(selector)
}

internal fun ViewElement.viewsMatching(target: ElementSelector): List<Pair<ViewElement, Int>> {
    val views = mutableListOf<Pair<ViewElement, Int>>()

    selector?.let {
        val weight = it.evaluateMatch(target)
        if (weight > 0) {
            views.add(Pair(this, weight))
        }
    }

    children?.let { items ->
        items.forEach { item ->
            views.addAll(item.viewsMatching(target))
        }
    }

    return views
}
