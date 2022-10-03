package com.appcues.ui.targetables

import android.view.View
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.data.Group
import androidx.compose.ui.unit.IntRect
import com.appcues.ui.targetables.TargetableElement.ComposableView
import com.appcues.ui.targetables.TargetableElement.XMLView

/**
 * Information about a Compose `LayoutNode`, extracted from a [Group] tree via [Group.layoutInfo].
 *
 * This is a useful layer of indirection from directly handling Groups because it allows us to
 * define our own notion of what an atomic unit of "composable" is independently from how Compose
 * actually represents things under the hood. When this changes in some future dev version, we
 * only need to update the "parsing" logic in this file.
 * It's also helpful since we actually gather data from multiple Groups for a single LayoutInfo,
 * so parsing them ahead of time into these objects means the visitor can be stateless.
 */
internal sealed class ComposeLayoutInfo {

    abstract val name: String
    abstract val bounds: IntRect

    data class LayoutNodeInfo(
        override val name: String,
        override val bounds: IntRect,
        val modifiers: List<Modifier>,
        val children: Sequence<ComposeLayoutInfo>,
    ) : ComposeLayoutInfo()

    data class ChildComposeLayoutInfo(
        override val name: String,
        override val bounds: IntRect,
        val children: Sequence<ComposeLayoutInfo>
    ) : ComposeLayoutInfo()

    data class AndroidViewInfo(
        override val name: String,
        override val bounds: IntRect,
        val view: View,
    ) : ComposeLayoutInfo()

    internal fun toTargetableElement(parent: TargetableElement? = null): TargetableElement =
        when (val layoutInfo = this) {
            is LayoutNodeInfo -> ComposableView(
                parent = parent,
                name = layoutInfo.name,
                bounds = layoutInfo.bounds,
                modifiers = layoutInfo.modifiers,
                childrenLayoutInfo = layoutInfo.children
            )
            is ChildComposeLayoutInfo -> ComposableView(
                parent = parent,
                name = layoutInfo.name,
                bounds = layoutInfo.bounds,
                childrenLayoutInfo = layoutInfo.children,
                modifiers = emptyList()
            )
            is AndroidViewInfo -> XMLView(
                parent = parent,
                name = layoutInfo.name,
                bounds = layoutInfo.bounds,
                view = layoutInfo.view,
            )
        }
}
