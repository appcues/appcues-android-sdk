package com.appcues.debugger.screencapture

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.getOrNull
import androidx.core.view.children
import com.appcues.ElementSelector
import com.appcues.ElementTargetingStrategy
import com.appcues.ViewElement
import com.appcues.data.MoshiConfiguration
import com.appcues.debugger.screencapture.AndroidViewSelector.Companion.SELECTOR_APPCUES_ID
import com.appcues.debugger.screencapture.AndroidViewSelector.Companion.SELECTOR_CONTENT_DESCRIPTION
import com.appcues.debugger.screencapture.AndroidViewSelector.Companion.SELECTOR_RESOURCE_NAME
import com.appcues.debugger.screencapture.AndroidViewSelector.Companion.SELECTOR_TAG
import com.appcues.isAppcuesView
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.ui.utils.getParentView
import com.appcues.util.withDensity
import com.squareup.moshi.Types
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.lang.reflect.Type

internal class AndroidViewSelector(
    private val properties: Map<String, String?>,
    val type: String? = null,
) : ElementSelector {

    companion object {

        const val SELECTOR_CONTENT_DESCRIPTION = "contentDescription"
        const val SELECTOR_TAG = "tag"
        const val SELECTOR_RESOURCE_NAME = "resourceName"
        const val SELECTOR_APPCUES_ID = "appcuesID"
    }

    private val contentDescription: String? = properties[SELECTOR_CONTENT_DESCRIPTION]?.ifEmpty { null }
    private val tag: String? = properties[SELECTOR_TAG]?.ifEmpty { null }
    private val resourceName: String? = properties[SELECTOR_RESOURCE_NAME]?.ifEmpty { null }
    private val appcuesId: String? = properties[SELECTOR_APPCUES_ID]?.ifEmpty { null }

    val isValid: Boolean
        get() = contentDescription != null || tag != null || resourceName != null || appcuesId != null

    val displayName: String?
        get() = when {
            resourceName != null -> resourceName
            appcuesId != null -> appcuesId
            tag != null -> "$type (tag $tag)"
            contentDescription != null -> "$type ($contentDescription)"
            else -> null
        }

    override fun toMap(): Map<String, String> {
        return properties.filterValues { !it.isNullOrEmpty() }.mapValues { it.value as String }
    }

    @Suppress("MagicNumber")
    override fun evaluateMatch(target: ElementSelector): Int {
        var weight = 0

        (target as? AndroidViewSelector)?.let {
            if (it.resourceName != null && it.resourceName == resourceName) {
                weight += 1_000
            }

            if (it.appcuesId != null && it.appcuesId == appcuesId) {
                weight += 1_000
            }

            if (it.tag != null && it.tag == tag) {
                weight += 100
            }

            if (it.contentDescription != null && it.contentDescription == contentDescription) {
                weight += 10
            }
        }

        return weight
    }
}

internal class AndroidTargetingStrategy : ElementTargetingStrategy {

    override suspend fun captureLayout(): ViewElement? {
        return AppcuesActivityMonitor.activity?.getParentView()?.let {
            val screenBounds = Rect()
            it.getGlobalVisibleRect(screenBounds)
            it.asCaptureView(screenBounds)
        }
    }

    override fun inflateSelectorFrom(properties: Map<String, String>): ElementSelector? {
        return AndroidViewSelector(properties).let { if (it.isValid) it else null }
    }
}

private const val ANDROID_COMPOSE_VIEW_CLASS_NAME = "androidx.compose.ui.platform.AndroidComposeView"

private suspend fun View.asCaptureView(screenBounds: Rect): ViewElement? {
    // the coordinates of the non-clipped area of this view in the coordinate space of the view's root view
    val globalVisibleRect = Rect()

    // ignore the Appcues SDK content that has been injected into the view hierarchy
    if (this.isAppcuesView() ||
        // if getGlobalVisibleRect returns false, that indicates that none of the view is
        // visible within the root view, and we will not include it in our capture
        getGlobalVisibleRect(globalVisibleRect).not() ||
        // if the view is not currently in the screenshot image (scrolled away), ignore
        // (this is possibly a redundant check to item above, but keeping for now)
        Rect.intersects(globalVisibleRect, screenBounds).not()
    ) {
        // if any of these conditions failed, this view is not captured
        return null
    }

    val children: MutableList<ViewElement> = mutableListOf()

    val rectDp = withDensity { globalVisibleRect.toDp() }

    // gather up child views for any ViewGroup
    // note: AndroidComposeView (handled below) is also a ViewGroup, and can have
    // additional View items within, when using Compose <--> View interop. Those
    // will also be collected here
    if (this is ViewGroup) {
        coroutineScope {
            val deferred = this@asCaptureView.children.map {
                async {
                    if (!it.isShown) {
                        // discard hidden views and subviews within
                        null
                    } else {
                        it.asCaptureView(screenBounds)
                    }
                }
            }.toList()

            val viewChildren = deferred.awaitAll().filterNotNull()
            children.addAll(viewChildren)
        }
    }

    // For an AndroidComposeView, we need to gather up the layout info for the Composables
    // within. This is done by accessing the semanticsOwner through reflection. At this time
    // there is no other way to access or create it (internal constructor). Using the
    // SemanticsNodes within allows us to traverse the view info to find selectable elements
    // very similar to how compose UI testing works.
    if (this::class.java.name == ANDROID_COMPOSE_VIEW_CLASS_NAME) {
        @Suppress("TooGenericExceptionCaught")
        try {
            val androidComposeViewClass = Class.forName(ANDROID_COMPOSE_VIEW_CLASS_NAME)
            // use getDeclaredField instead of getField due to private access
            val semanticsOwnerField = androidComposeViewClass.getDeclaredField("semanticsOwner")
                .apply { isAccessible = true } // make private filed accessible
            val semanticsOwner = semanticsOwnerField.get(this) as SemanticsOwner
            semanticsOwner.rootSemanticsNode.asCaptureView(context, screenBounds)?.let { children.add(it) }
        } catch (ex: Exception) {
            // Catching and swallowing exceptions here with the Compose view handling in case
            // something changes in the future that breaks the expected structure being accessed
            // through reflection here. If anything goes wrong within this block, prefer to continue
            // processing the remainder of the view tree as best we can.
            Log.e("Appcues", "error processing Compose layout, ${ex.message}")
        }
    }

    if (this is WebView) {
        val js = """
        [...document.querySelectorAll('button')].map (el => {
            const { x, y, width, height } = el.getBoundingClientRect();
            return {
                x,
                y,
                width,
                height,
                selector: `html-${'$'}{el.id}`,
            }
        });
        """

        val result = evaluateJavascript(js)
        val viewChildren = result.map { el ->
            val x = (el["x"] as Double).toInt()
            val y = (el["y"] as Double).toInt()
            val width = (el["width"] as Double).toInt()
            val height = (el["height"] as Double).toInt()
            ViewElement(
                x = rectDp.left + x,
                y = rectDp.top + y,
                width = width,
                height = height,
                displayName = null,
                selector = AndroidViewSelector(
                    properties = mapOf(SELECTOR_APPCUES_ID to (el["selector"] as String)),
                ),
                type = "htmlNode",
                children = null,
            )
        }

        children.addAll(viewChildren)
    }

    return selector(globalVisibleRect).let {
        ViewElement(
            x = rectDp.left,
            y = rectDp.top,
            width = rectDp.width(),
            height = rectDp.height(),
            displayName = it?.displayName,
            selector = it,
            type = it?.type ?: this::class.java.simpleName,
            children = children.ifEmpty { null },
        )
    }
}

private suspend fun WebView.evaluateJavascript(script: String) : List<Map<String, Any>> {
    val completion = CompletableDeferred<List<Map<String, Any>>>()
    evaluateJavascript(script) { result ->
        val listType: Type = Types.newParameterizedType(List::class.java, Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))
        val adapter = MoshiConfiguration.moshi.adapter<List<Map<String, Any>>>(listType)
        val parsed: List<Map<String, Any>> = adapter.fromJson(result)!!
        completion.complete(parsed)
    }
    return completion.await()
}

private fun View.isVisibleForTargeting(globalVisibleRect: Rect): Boolean {
    // considering it eligible for targeting if the center point is visible
    val actualHeight = measuredHeight.toDouble()
    val actualWidth = measuredWidth.toDouble()
    val centerX = (globalVisibleRect.left + (actualWidth / 2.0)).toInt()
    val centerY = (globalVisibleRect.top + (actualHeight / 2.0)).toInt()
    return globalVisibleRect.contains(centerX, centerY)
}

private fun View.selector(globalVisibleRect: Rect): AndroidViewSelector? {
    if (!isVisibleForTargeting(globalVisibleRect)) return null

    return AndroidViewSelector(
        properties = mapOf(
            SELECTOR_CONTENT_DESCRIPTION to contentDescription?.toString(),
            SELECTOR_TAG to tag?.toString(),
            SELECTOR_RESOURCE_NAME to extractResourceName()
        ),
        type = this::class.java.simpleName,
    ).let { if (it.isValid) it else null }
}

@Suppress("SwallowedException")
private fun View.extractResourceName(): String? {
    return try {
        if (this.isClickable && this !is WebView) resources.getResourceEntryName(id) else null
    } catch (_: NotFoundException) {
        null
    }
}

private fun SemanticsNode.asCaptureView(context: Context, screenBounds: Rect): ViewElement? {
    val bounds = unclippedGlobalBounds()

    // if the view is not currently in the screenshot image (scrolled away), ignore
    if (Rect.intersects(bounds, screenBounds).not()) {
        return null
    }

    val childElements: List<ViewElement> = children.mapNotNull {
        it.asCaptureView(context, screenBounds)
    }

    return selector(bounds, screenBounds).let {
        val boundsDp = context.withDensity { bounds.toDp() }
        ViewElement(
            x = boundsDp.left,
            y = boundsDp.top,
            width = boundsDp.width(),
            height = boundsDp.height(),
            displayName = it?.displayName,
            selector = it,
            type = it?.type ?: "Composable #$id",
            children = childElements.ifEmpty { null },
        )
    }
}

private fun SemanticsNode.unclippedGlobalBounds(): Rect =
    Rect(
        positionInWindow.x.toInt(),
        positionInWindow.y.toInt(),
        positionInWindow.x.toInt() + size.width,
        positionInWindow.y.toInt() + size.height
    )

private val AppcuesViewTagKey = SemanticsPropertyKey<String>("AppcuesViewTagKey")

// used by the public appcuesViewTag Modifier in ElementTargetingStrategy.kt provided by SDK
internal var SemanticsPropertyReceiver.appcuesViewTagProperty by AppcuesViewTagKey

private fun SemanticsNode.selector(bounds: Rect, screenBounds: Rect): AndroidViewSelector? {
    // the view center point must be within the screen bounds to be eligible for targeting
    // this is the Compose version of View.isVisibleForTargeting() that is done with the visible
    // rect of an Android.view.View.
    val centerX = bounds.left + (bounds.width() / 2)
    val centerY = bounds.top + (bounds.height() / 2)
    if (!screenBounds.contains(centerX, centerY)) {
        return null
    }

    // we can look up the view tag set by the Modifier here and use it for our selector
    if (config.contains(AppcuesViewTagKey)) {
        return AndroidViewSelector(
            properties = mapOf(SELECTOR_APPCUES_ID to config[AppcuesViewTagKey]),
            type = config.getOrNull(SemanticsProperties.Role)?.toString()
        )
    }
    return null
}
