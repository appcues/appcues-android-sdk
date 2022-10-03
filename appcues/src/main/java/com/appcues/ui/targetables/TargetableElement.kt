package com.appcues.ui.targetables

import android.content.res.Resources.NotFoundException
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntRect
import com.appcues.R
import com.appcues.ui.targetables.AppcuesViewScanner.toXMLView
import com.appcues.ui.targetables.TargetableElement.ComposableView
import com.appcues.ui.targetables.TargetableElement.XMLView

/**
 * Represents a logic view that can be rendered as a node in the view tree.
 *
 * Can either be an actual Android [View] ([XMLView]) or a grouping of Composables that roughly
 * represents the concept of a logical "view" ([ComposableView]).
 */
internal sealed class TargetableElement {

    abstract val parent: TargetableElement?

    abstract val name: String

    abstract val bounds: IntRect

    abstract val isTouchable: Boolean

    abstract val children: Sequence<TargetableElement>

    fun findElementByXPath(xPath: String): TargetableElement? {
        if (xPath.isEmpty()) return this

        val slicedXPath = xPath.split("/")

        slicedXPath.firstOrNull()?.let {
            val nextXPath = if (slicedXPath.size > 1) {
                xPath.removePrefix("$it/")
            } else {
                ""
            }

            if (it.startsWith("@") && this is XMLView) {
                return findByIdName(it)?.findElementByXPath(nextXPath)
            } else {

                var name = it
                val index = if (it.matches(Regex(".*\\[\\d+]\$"))) {
                    // clean name in case there is number index
                    name = it.split("[")[0]
                    // grab number from xPath
                    it.removeSuffix("]").split("[")[1].toInt()
                    // or default to the first element of that Name
                } else 0

                return children.filter { element -> element.name == name }.elementAtOrNull(index)?.findElementByXPath(nextXPath)
            }
        }
        return null
    }

    class XMLView(
        override val parent: TargetableElement?,
        override val name: String,
        override val bounds: IntRect,
        val view: View,
    ) : TargetableElement() {

        override val isTouchable: Boolean
            get() = view.isFocusable && view.mightBeComposeView.not()

        private val idName: String? = getIdName()

        override val children: Sequence<TargetableElement> = elementChildren()

        fun findByIdName(idName: String): TargetableElement? {
            // check to see if the idName is self
            if (this.idName == idName) return this

            // iterate through children of type XMLView and checks whether one of those is or its children is a match for this idName
            return children.filter { it is XMLView }.map { (it as XMLView).findByIdName(idName) }.filterNotNull().firstOrNull()
        }

        private fun getIdName(): String? {
            if (view.id > 0 && view.resources != null) {
                return try {
                    val packageName: String = when (view.id and -0x1000000) {
                        0x7f000000 -> "app"
                        0x01000000 -> "android"
                        else -> view.resources.getResourcePackageName(view.id)
                    }
                    val entryName = view.resources.getResourceEntryName(view.id)
                    "@$packageName:$entryName"
                } catch (e: NotFoundException) {
                    Log.e("Appcues", "Error: ${e.message}")
                    null
                }
            }
            return null
        }

        private fun elementChildren(): Sequence<TargetableElement> = sequence {
            if (view.mightBeComposeView) {
                val (composableViews, parsedComposables) = getComposeScannableViews(this@XMLView)
                // If unsuccessful, the list will contain a RenderError, so yield it anyway.
                yieldAll(composableViews)
                if (parsedComposables) {
                    // Don't visit children ourselves, the compose renderer will have done that.
                    return@sequence
                }
            }

            // don't proceed in case view is not ViewGroup or if its one of our internal views
            if (view !is ViewGroup || (view.id == R.id.appcues_debugger_view || view.id == R.id.appcues_overlay_view)) return@sequence

            for (i in 0 until view.childCount) {
                // Child may be null, if children were removed by another thread after we captured the child
                // count. getChildAt returns null for invalid indices, it doesn't throw.
                val child = view.getChildAt(i) ?: continue
                yield(child.toXMLView(this@XMLView))
            }
        }

        override fun toString(): String = "${XMLView::class.java.simpleName}($name) - $idName"
    }

    /**
     * Represents a group of Composables that make up a logical "view".
     *
     * @param modifiers The list of [Modifier]s that are currently applied to the Composable.
     */
    class ComposableView(
        childrenLayoutInfo: Sequence<ComposeLayoutInfo>,
        override val parent: TargetableElement?,
        override val name: String,
        override val bounds: IntRect,
        val modifiers: List<Modifier>,
    ) : TargetableElement() {

        override val isTouchable: Boolean = modifiers.toString().contains("FocusModifier")

        override val children: Sequence<TargetableElement> = childrenLayoutInfo.map { it.toTargetableElement(this) }

        override fun toString(): String = "${ComposableView::class.java.simpleName}($name) - Modifiers: $modifiers"
    }

    /**
     * Indicates that an exception was thrown while rendering part of the tree.
     * This should be used for non-fatal errors, when the rest of the tree should still be processed.
     *
     * By default, exceptions thrown during rendering will abort the entire rendering process, and
     * return the error message along with any portion of the tree that was rendered before the
     * exception was thrown.
     */
    class ChildRenderingError(
        override val parent: TargetableElement?,
        private val message: String
    ) : TargetableElement() {

        override val name: String get() = message
        override val bounds: IntRect = IntRect(0, 0, 0, 0)
        override val isTouchable: Boolean = false
        override val children: Sequence<TargetableElement> get() = emptySequence()
    }
}
