package com.appcues.ui.targetables

import android.view.View
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Composition
import com.appcues.ui.targetables.TargetableElement.ChildRenderingError
import com.appcues.ui.targetables.TargetableElement.XMLView

private const val WRAPPED_COMPOSITION_CLASS_NAME = "androidx.compose.ui.platform.WrappedComposition"
private const val COMPOSITION_IMPL_CLASS_NAME = "androidx.compose.runtime.CompositionImpl"

/**
 * True if this view looks like the private view type that Compose uses to host compositions.
 * It does a fuzzy match to try to detect unsupported Compose versions, which will not be rendered
 * but will at least warn that the version is unsupported.
 */
internal val View.mightBeComposeView: Boolean
    get() = "AndroidComposeView" in this::class.java.name

/**
 * If this is a `WrappedComposition`, returns the composition being wrapped, else returns this.
 */
internal fun Composition.unwrap(): Composition {
    if (this::class.java.name != WRAPPED_COMPOSITION_CLASS_NAME) return this
    val wrappedClass = Class.forName(WRAPPED_COMPOSITION_CLASS_NAME)
    val originalField = wrappedClass.getDeclaredField("original")
        .apply { isAccessible = true }
    return originalField.get(this) as Composition
}

/**
 * Tries to pull a [Composer] out of this [Composition], or returns null if it can't find one.
 */
internal fun Composition.getComposerOrNull(): Composer? {
    if (this::class.java.name != COMPOSITION_IMPL_CLASS_NAME) return null
    val compositionImplClass = Class.forName(COMPOSITION_IMPL_CLASS_NAME)
    val composerField = compositionImplClass.getDeclaredField("composer")
        .apply { isAccessible = true }
    return composerField.get(this) as? Composer
}

/**
 * Tries to extract a list of [TargetableElement.ComposableView]s from [TargetableElement.XMLView], which must be a view for which
 * [mightBeComposeView] is true. Returns the list of views and a boolean indicating whether the
 * extraction was successful.
 *
 * There is no public API for this, so this function uses reflection. If the reflection fails, or
 * the right Compose artifacts aren't on the classpath, or the runtime Compose version is
 * unsupported, this function will return a [ChildRenderingError] and false.
 */
internal fun getComposeScannableViews(xmlView: XMLView): Pair<List<TargetableElement>, Boolean> {
    var linkageError: LinkageError? = null
    val scannableViews = try {
        xmlView.tryGetLayoutInfo()
            ?.map { it.toTargetableElement(xmlView) }
            ?.toList()
    } catch (e: LinkageError) {
        // The view looks like an AndroidComposeView, but the Compose code on the classpath is
        // not what we expected – the app is probably using a newer (or older) version of Compose than
        // we support.
        linkageError = e
        null
    }
    // If we were able to successfully construct the LayoutInfo, then we assume the Compose version
    // is supported. or else create a ChildRenderingError
    return scannableViews?.let { Pair(it, true) } ?: Pair(listOf(composeRenderingError(xmlView, linkageError)), false)
}

/**
 * Returns a [ChildRenderingError] that includes an error message.
 */
internal fun composeRenderingError(parent: TargetableElement, exception: LinkageError?): TargetableElement {
    return ChildRenderingError(parent, "Error: $exception")
}
