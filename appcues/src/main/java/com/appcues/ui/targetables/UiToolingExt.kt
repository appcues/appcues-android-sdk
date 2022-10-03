package com.appcues.ui.targetables

import android.annotation.SuppressLint
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.node.Ref
import androidx.compose.ui.tooling.data.CallGroup
import androidx.compose.ui.tooling.data.Group
import androidx.compose.ui.tooling.data.NodeGroup
import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.compose.ui.tooling.data.asTree
import androidx.compose.ui.unit.IntRect
import com.appcues.ui.targetables.ComposeLayoutInfo.AndroidViewInfo
import com.appcues.ui.targetables.ComposeLayoutInfo.ChildComposeLayoutInfo
import com.appcues.ui.targetables.ComposeLayoutInfo.LayoutNodeInfo
import com.appcues.ui.targetables.TargetableElement.XMLView
import java.lang.reflect.Field
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 * Uses reflection to try to pull a `SlotTable` out of Composition and render it. If any of the
 * reflection fails, returns false.
 */
@OptIn(UiToolingDataApi::class)
internal fun XMLView.tryGetLayoutInfo(): Sequence<ComposeLayoutInfo>? {
    // Any of this reflection code can fail if running with an unsupported version of Compose.
    // Compose doesn't provide a public API for this (yet) because they don't want it to be used in production.
    val keyedTags = view.getKeyedTags()
    val composition = keyedTags.first { it is Composition } as Composition? ?: return null
    val composer = composition.unwrap()
        .getComposerOrNull() ?: return null

    // asTree is provided by the Compose Tooling library. It "reads" the slot table and parses it
    // into a tree of Group objects. This means we're technically traversing the composable tree
    // twice, so why not just read the slot table directly? As opaque as the Group API is, the actual
    // slot table API is quite complicated, and the actual format of the slot table (effectively an
    // array that stores a flattened version of a composition tree) is super low level. It's likely to
    // change a lot between compose versions, and keeping up with that with every two-week dev release
    // would be a lot of work. Additionally, a lot of the objects stored in the slot table are not
    // public (eg LayoutNode), so we'd need to use even more (brittle) reflection to do that parsing.
    // That said, once Compose is more stable, it might be worth it to read the slot table directly,
    // since then we could drop the requirement for the Tooling library to be on the classpath.
    return composer.compositionData.asTree().layoutInfo
}

private fun View.getKeyedTags(): SparseArray<*> {
    return viewKeyedTagsField?.get(this) as SparseArray<*>? ?: SparseArray<Nothing>(0)
}

private val viewKeyedTagsField: Field? by lazy(PUBLICATION) { getKeyedTagsProperty() }

@SuppressLint("DiscouragedPrivateApi")
private fun getKeyedTagsProperty(): Field? {
    return try {
        View::class.java.getDeclaredField("mKeyedTags")
            .apply { isAccessible = true }
    } catch (e: NoSuchFieldException) {
        Log.e("Appcues", "Field not found: ${e.message}")
        // Some devices don't have this field apparently.
        // See https://github.com/square/radiography/issues/119.
        null
    }
}

private inline fun SparseArray<*>.first(predicate: (Any?) -> Boolean): Any? {
    for (i in 0 until size()) {
        val item = valueAt(i)
        if (predicate(item)) return item
    }
    return null
}

/**
 * A sequence that lazily parses [ComposeLayoutInfo]s from a [Group] tree.
 */
@OptIn(UiToolingDataApi::class)
internal val Group.layoutInfo: Sequence<ComposeLayoutInfo>
    get() = computeLayoutInfo()

/**
 * Recursively parses [ComposeLayoutInfo]s from a [Group]. Groups form a tree and can contain different
 * type of nodes which represent function calls, arbitrary data stored directly in the slot table,
 * or just subtrees.
 *
 * This function walks the tree and collects only Groups which represent emitted values
 * ([NodeGroup]s). These either represent `LayoutNode`s (Composer's internal primitive for layout
 * algorithms) or classic Android views that the composition emitted. This function collapses all
 * the groups in between each of these nodes, but uses the top-most Group under the previous node
 * to derive the "name" of the [ComposeLayoutInfo]. The other [ComposeLayoutInfo] properties come directly off
 * [NodeGroup] values.
 */
@OptIn(UiToolingDataApi::class)
@Suppress("ReturnCount")
private fun Group.computeLayoutInfo(
    parentName: String = ""
): Sequence<ComposeLayoutInfo> {
    val name = parentName.ifBlank { this.name }.orEmpty()
    // Things that we want to consider children of the current node, but aren't actually child nodes
    // as reported by Group.children.
    val irregularChildren = subComposedChildren(name) + androidViewChildren()

    // Certain composables produce an internal structure that is hard to read if we report it exactly.
    // Instead, we use heuristics to recognize subtrees that match certain expected structures and
    // aggregate them somewhat before reporting.
    tryParseSubcomposition(name, irregularChildren)
        ?.let { return it }
    tryParseAndroidView(name, irregularChildren)
        ?.let { return it }

    // This is an intermediate group that doesn't represent a LayoutNode, so we flatten by just
    // reporting its children without reporting a new subtree.
    if (this !is NodeGroup) {
        return children.asSequence()
            .flatMap { it.computeLayoutInfo(name) } + irregularChildren
    }

    val children = children.asSequence()
        // This node will "consume" the name, so reset it name to empty for children.
        .flatMap { it.computeLayoutInfo() }

    val layoutInfo = LayoutNodeInfo(
        name = name,
        bounds = box,
        modifiers = modifierInfo.map { it.modifier },
        children = children + irregularChildren,
    )
    return sequenceOf(layoutInfo)
}

/**
 * Look for any `CompositionContext`s stored in this group. These will be rolled up into the
 * `SubComposeLayout` if present, otherwise they will just be shown as regular children.
 * The compositionData val is marked as internal, and not intended for public consumption.
 * The returned [ChildComposeLayoutInfo]s should be collated by [tryParseSubcomposition].
 */
@OptIn(UiToolingDataApi::class)
private fun Group.subComposedChildren(name: String): Sequence<ChildComposeLayoutInfo> =
    getCompositionContexts()
        .flatMap { it.tryGetComposers().asSequence() }
        .map { subComposer ->
            ChildComposeLayoutInfo(
                name = name,
                bounds = box,
                children = subComposer.compositionData.asTree().layoutInfo
            )
        }

/**
 * The `AndroidView` composable remembers a [Ref] to a special internal subclass of [ViewGroup] that
 * manages the wiring between the hosting android view and the child view. This function looks for
 * refs to views and returns them as [AndroidViewInfo]s to be collated with [tryParseAndroidView].
 *
 * Note that [Ref] is a public type – any third-party composable could also remember a ref to a
 * view, and it would be reported by this function. That would almost certainly be a code smell for
 * a number of reasons though, so we don't try to ignore those cases.
 */
@OptIn(UiToolingDataApi::class)
private fun Group.androidViewChildren(): List<AndroidViewInfo> = data.mapNotNull { datum ->
    (datum as? Ref<*>)
        ?.value
        // The concrete type is actually an internal ViewGroup subclass that has all the wiring, but
        // ultimately it's still just a ViewGroup so this simple check works.
        ?.let { it as? ViewGroup }
        ?.let {
            AndroidViewInfo(
                it::class.java.simpleName,
                IntRect(it.left, it.top, it.right, it.bottom),
                it
            )
        }
}

/**
 * SubcomposeLayouts need to be handled specially, because all their subcompositions are always
 * logical children of their single LayoutNode. In order to render them so that the rendering
 * actually matches that logical structure, we need to reorganize the subtree a bit so
 * subcompositions are children of the layout node and not siblings of it.
 *
 * Note that there's no sure-fire way to actually detect a SubcomposeLayout. The best we can do is
 * use a heuristic. If any part of the heuristics don't match, then we fall back to treating the
 * group like any other.
 *
 * The heuristic we use is:
 * - Name of the group is "SubcomposeLayout".
 * - Has one or more subcompositions under it.
 * - Has exactly one LayoutNode child.
 * - That LayoutNode has no children of its own.
 */
@OptIn(UiToolingDataApi::class)
@Suppress("ReturnCount")
private fun Group.tryParseSubcomposition(
    name: String,
    irregularChildren: Sequence<ComposeLayoutInfo>
): Sequence<ComposeLayoutInfo>? {
    if (this.name != "SubcomposeLayout") return null

    val (subcompositions, regularChildren) =
        (children.asSequence().flatMap { it.computeLayoutInfo(name) } + irregularChildren)
            .partition { it is ChildComposeLayoutInfo }
            .let {
                // There's no type-safe partition operator so we just cast.
                @Suppress("UNCHECKED_CAST")
                it as Pair<List<ChildComposeLayoutInfo>, List<ComposeLayoutInfo>>
            }

    if (subcompositions.isEmpty()) return null
    if (regularChildren.size != 1) return null

    val mainNode = regularChildren.single()
    if (mainNode !is LayoutNodeInfo) return null
    if (!mainNode.children.iterator().hasNext().not()) return null

    // We can be pretty confident at this point that this is an actual SubcomposeLayout, so
    // expose its layout node as the parent of all its subcompositions.
    val subcompositionName = "<subcomposition of ${mainNode.name}>"
    return sequenceOf(
        mainNode.copy(
            children = subcompositions.asSequence().map { it.copy(name = subcompositionName) }
        )
    )
}

/**
 * The AndroidView composable also needs to be special-cased. The actual android view is stored
 * in a Ref deep inside the hierarchy somewhere, but we want to expose it as the immediate child
 * of nearest common parent node that contains both the android view and the LayoutNode that is
 * used as a proxy to measure and lay it out in the composable.
 *
 * We can't rely on just the composable name, since any composable could be called "AndroidView",
 * so if any of the subtree parsing fails to match our expectations, we fallback to treating it
 * like any other group. Note that this heuristic isn't as strict as the subcomposition one, since
 * there's only one way to get an android view into a composition, so we can rely more heavily on
 * the presence of an actual android view. We still require there to be only one LayoutNode child,
 * otherwise it would be ambiguous which node we should report as the parent of the view.
 * We also require the common parent to be a CallGroup, since that is a valid assumption as of the
 * time of this writing and it saves us the additional logic of having to decide whether to return
 * this or the mainNode as the root of the subtree if this is a NodeGroup for some reason.
 *
 * Note that while this looks very similar to the [tryParseSubcomposition], that is probably
 * mostly coincidental, so it's probably not a good idea to factor out any abstractions. Since
 * they both rely on internal-only implementation details of how the Compose runtime happens to
 * work, either of them could change independently in the future, and it will be easier to update
 * the logic of both if that happens if they're completely independent.
 */
@OptIn(UiToolingDataApi::class)
@Suppress("ReturnCount")
private fun Group.tryParseAndroidView(
    name: String,
    irregularChildren: Sequence<ComposeLayoutInfo>
): Sequence<ComposeLayoutInfo>? {
    if (this.name != "AndroidView") return null
    if (this !is CallGroup) return null

    val (androidViews, regularChildren) =
        (children.asSequence().flatMap { it.computeLayoutInfo(name) } + irregularChildren)
            .partition { it is AndroidViewInfo }
            .let {
                // There's no type-safe partition operator so we just cast.
                @Suppress("UNCHECKED_CAST")
                it as Pair<List<AndroidViewInfo>, List<ComposeLayoutInfo>>
            }

    if (androidViews.isEmpty()) return null
    if (regularChildren.size != 1) return null

    val mainNode = regularChildren.single()
    if (mainNode !is LayoutNodeInfo) return null

    // We can be pretty confident at this point that this is an actual AndroidView composable,
    // so expose its layout node as the parent of its actual view.
    return sequenceOf(mainNode.copy(children = mainNode.children + androidViews))
}

@OptIn(UiToolingDataApi::class)
internal fun Group.getCompositionContexts(): Sequence<CompositionContext> {
    return REFLECTION_CONSTANTS?.run {
        data.asSequence()
            .filter { it != null && it::class.java == CompositionContextHolderClass }
            .mapNotNull { holder -> holder.tryGetCompositionContext() }
    } ?: emptySequence()
}

@Suppress("UNCHECKED_CAST")
internal fun CompositionContext.tryGetComposers(): Iterable<Composer> {
    return REFLECTION_CONSTANTS?.let {
        if (!it.CompositionContextImplClass.isInstance(this)) return emptyList()
        it.CompositionContextImplComposersField.get(this) as? Iterable<Composer>
    } ?: emptyList()
}

private fun Any?.tryGetCompositionContext() = REFLECTION_CONSTANTS?.let {
    it.CompositionContextHolderRefField.get(this) as? CompositionContext
}

private val REFLECTION_CONSTANTS by lazy(PUBLICATION) {
    try {
        object {
            val CompositionContextHolderClass =
                Class.forName("androidx.compose.runtime.ComposerImpl\$CompositionContextHolder")
            val CompositionContextImplClass =
                Class.forName("androidx.compose.runtime.ComposerImpl\$CompositionContextImpl")
            val CompositionContextHolderRefField =
                CompositionContextHolderClass.getDeclaredField("ref")
                    .apply { isAccessible = true }
            val CompositionContextImplComposersField =
                CompositionContextImplClass.getDeclaredField("composers")
                    .apply { isAccessible = true }
        }
    } catch (e: ClassNotFoundException) {
        Log.e("Appcues", "Class not found $e")
        null
    } catch (e: NoSuchFieldException) {
        Log.e("Appcues", "Field not found $e")
        null
    }
}
